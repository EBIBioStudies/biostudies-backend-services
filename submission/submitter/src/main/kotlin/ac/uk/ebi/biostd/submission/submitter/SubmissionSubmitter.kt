package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.CollectionInfoService
import ac.uk.ebi.biostd.submission.service.CollectionRequest
import ac.uk.ebi.biostd.submission.service.CollectionResponse
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.from.toExtAttribute
import ebi.ac.uk.extended.mapping.from.toExtSection
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.AccNumber
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.SUBMISSION_RESERVED_ATTRIBUTES
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.date.isBeforeOrEqual
import mu.KotlinLogging
import java.time.OffsetDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}
private const val DEFAULT_VERSION = 1

@Suppress("TooManyFunctions")
class SubmissionSubmitter(
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val parentInfoService: ParentInfoService,
    private val collectionInfoService: CollectionInfoService,
    private val submissionRequestService: SubmissionRequestService,
    private val queryService: SubmissionMetaQueryService
) {
    fun submit(request: SubmissionRequest): ExtSubmission {
        logger.info { "processing request $request" }
        val submission = process(
            request.submission,
            request.submitter.asUser(),
            request.onBehalfUser?.asUser(),
            request.sources,
            request.method)

        logger.info { "Saving submission ${submission.accNo}" }
        val saveRequest = SaveSubmissionRequest(submission, request.mode, request.draftKey)

        return submissionRequestService.saveAndProcessSubmissionRequest(saveRequest)
    }

    fun processRequest(request: SaveSubmissionRequest): ExtSubmission {
        logger.info { "processing request for submission ${request.submission.accNo} " }
        return submissionRequestService.processSubmission(request)
    }

    fun submitAsync(request: SubmissionRequest): SaveSubmissionRequest {
        logger.info { "processing async request $request" }

        val submission = process(
            request.submission,
            request.submitter.asUser(),
            request.onBehalfUser?.asUser(),
            request.sources,
            request.method)

        logger.info { "Saving submission request ${submission.accNo}" }
        val saveRequest = SaveSubmissionRequest(submission, request.mode, request.draftKey)
        val persistedRequest = submissionRequestService.saveSubmissionRequest(saveRequest)

        return SaveSubmissionRequest(persistedRequest, request.mode, request.draftKey)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun process(
        submission: Submission,
        submitter: User,
        onBehalfUser: User?,
        source: FilesSource,
        method: SubmissionMethod
    ): ExtSubmission {
        try {
            val extSubmission = processSubmission(submission, submitter, onBehalfUser, source, method)
            parentInfoService.executeCollectionValidators(extSubmission)

            return extSubmission
        } catch (exception: RuntimeException) {
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }

    private fun processSubmission(
        submission: Submission,
        submitter: User,
        onBehalfUser: User?,
        source: FilesSource,
        method: SubmissionMethod
    ): ExtSubmission {
        val previousVersion = queryService.findLatestBasicByAccNo(submission.accNo)
        val isNew = previousVersion == null
        val (parentTags, parentReleaseTime, parentPattern) = parentInfoService.getParentInfo(submission.attachTo)
        val (createTime, modTime, releaseTime) = getTimes(submission, previousVersion?.creationTime, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val accNo = getAccNumber(submission, isNew, submitter, parentPattern)
        val accNoString = accNo.toString()
        val collectionInfo = getCollectionInfo(submitter, submission, accNoString, isNew)
        val secretKey = previousVersion?.secretKey ?: UUID.randomUUID().toString()
        val relPath = accNoService.getRelPath(accNo)
        val tags = getTags(parentTags, collectionInfo)
        val ownerEmail = onBehalfUser?.email ?: previousVersion?.owner ?: submitter.email

        return ExtSubmission(
            accNo = accNoString,
            owner = ownerEmail,
            submitter = submitter.email,
            version = DEFAULT_VERSION,
            method = getMethod(method),
            title = submission.title,
            relPath = relPath,
            rootPath = submission.rootPath,
            released = released,
            secretKey = secretKey,
            status = ExtProcessingStatus.PROCESSED,
            releaseTime = releaseTime,
            modificationTime = modTime,
            creationTime = createTime,
            tags = submission.tags.map { ExtTag(it.first, it.second) },
            collections = tags.map { ExtCollection(it) },
            section = submission.section.toExtSection(source),
            attributes = getAttributes(submission)
        )
    }

    private fun getMethod(method: SubmissionMethod): ExtSubmissionMethod {
        return when (method) {
            SubmissionMethod.FILE -> ExtSubmissionMethod.FILE
            SubmissionMethod.PAGE_TAB -> ExtSubmissionMethod.PAGE_TAB
            SubmissionMethod.UNKNOWN -> ExtSubmissionMethod.UNKNOWN
        }
    }

    private fun getTags(parentTags: List<String>, collection: CollectionResponse?): List<String> {
        val tags = parentTags.filter { it != PUBLIC_ACCESS_TAG.value }.toMutableList()
        if (collection != null) tags.add(collection.accessTag)
        return tags
    }

    private fun getCollectionInfo(user: User, sub: Submission, accNo: String, isNew: Boolean): CollectionResponse? {
        val request = CollectionRequest(user.email, sub.section.type, sub.accNoTemplate, accNo, isNew)
        return collectionInfoService.process(request)
    }

    private fun getAttributes(submission: Submission) = submission.attributes
        .filterNot { SUBMISSION_RESERVED_ATTRIBUTES.contains(it.name) }
        .map { it.toExtAttribute() }

    private fun getAccNumber(sub: Submission, isNew: Boolean, user: User, parentPattern: String?): AccNumber {
        val accNo = sub.accNo.ifBlank { null }
        val request = AccNoServiceRequest(user.email, accNo, isNew, sub.attachTo, parentPattern)

        return accNoService.calculateAccNo(request)
    }

    private fun getTimes(sub: Submission, creationTime: OffsetDateTime?, parentReleaseTime: OffsetDateTime?) =
        timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, creationTime, parentReleaseTime))
}
