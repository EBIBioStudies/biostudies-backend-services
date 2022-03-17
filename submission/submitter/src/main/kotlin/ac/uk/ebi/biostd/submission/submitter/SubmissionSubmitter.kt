package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.CollectionInfoService
import ac.uk.ebi.biostd.submission.service.CollectionRequest
import ac.uk.ebi.biostd.submission.service.CollectionResponse
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.from.ToExtSectionMapper
import ebi.ac.uk.extended.mapping.from.toExtAttribute
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
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
private const val DEFAULT_SCHEMA_VERSION = "1.0"

@Suppress("TooManyFunctions", "LongParameterList")
class SubmissionSubmitter(
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val parentInfoService: ParentInfoService,
    private val collectionInfoService: CollectionInfoService,
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val queryService: SubmissionMetaQueryService,
    private val submissionQueryService: SubmissionQueryService,
    private val draftService: SubmissionDraftService,
    private val properties: ApplicationProperties,
    private val toExtSectionMapper: ToExtSectionMapper
) {
    fun submitAsync(rqt: SubmitRequest): Pair<String, Int> {
        logger.info { "${rqt.accNo} ${rqt.submitter.email} Processing async request $rqt" }
        val sub = process(rqt.submission, rqt.submitter.asUser(), rqt.onBehalfUser?.asUser(), rqt.sources, rqt.method)
        logger.info { "${sub.accNo} ${sub.submitter} Saving submission request ${sub.accNo}" }
        return saveRequest(SubmissionRequest(sub, rqt.mode, rqt.draftKey), rqt.owner)
    }

    fun submitAsync(request: SubmissionRequest): Pair<String, Int> = saveRequest(request, request.submission.submitter)

    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val saveRequest = submissionQueryService.getPendingRequest(accNo, version)
        val submitter = saveRequest.submission.submitter
        logger.info { "$accNo, $submitter Processing request for submission accNo='$accNo', version='$version'" }
        return submissionPersistenceService.processSubmissionRequest(saveRequest)
    }

    fun release(request: ReleaseRequest) {
        val (accNo, owner, relPath) = request
        submissionPersistenceService.releaseSubmission(accNo, owner, relPath)
    }

    private fun saveRequest(request: SubmissionRequest, owner: String): Pair<String, Int> {
        val saved = submissionPersistenceService.saveSubmissionRequest(request)
        request.draftKey?.let { draftService.setProcessingStatus(owner, it) }
        return saved
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
            logger.error(exception) { "Error processing submission request accNo='${submission.accNo}'" }
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
            schemaVersion = DEFAULT_SCHEMA_VERSION,
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
            section = toExtSectionMapper.convert(submission.section, source),
            attributes = getAttributes(submission),
            storageMode = if (properties.persistence.enableFire) FIRE else NFS
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

    private fun getAttributes(submission: Submission): List<ExtAttribute> {
        return submission.attributes
            .filterNot { SUBMISSION_RESERVED_ATTRIBUTES.contains(it.name) }
            .map { it.toExtAttribute() }
    }

    private fun getAccNumber(sub: Submission, isNew: Boolean, user: User, parentPattern: String?): AccNumber {
        val accNo = sub.accNo.ifBlank { null }
        val request = AccNoServiceRequest(user.email, accNo, isNew, sub.attachTo, parentPattern)
        return accNoService.calculateAccNo(request)
    }

    private fun getTimes(sub: Submission, creationTime: OffsetDateTime?, parentReleaseTime: OffsetDateTime?) =
        timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, creationTime, parentReleaseTime))
}
