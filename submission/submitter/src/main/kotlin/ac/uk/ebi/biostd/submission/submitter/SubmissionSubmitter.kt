package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SaveRequest
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.ProjectInfoService
import ac.uk.ebi.biostd.submission.service.ProjectRequest
import ac.uk.ebi.biostd.submission.service.ProjectResponse
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.from.toExtAttribute
import ebi.ac.uk.extended.mapping.from.toExtSection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.Project
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.RESERVED_ATTRIBUTES
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
    private val projectInfoService: ProjectInfoService,
    private val context: PersistenceContext,
    private val queryService: SubmissionQueryService
) {
    fun submit(request: SubmissionRequest): ExtSubmission {
        logger.info { "processing request $request" }

        val submission = process(
            request.submission,
            request.submitter.asUser(),
            request.onBehalfUser?.asUser(),
            request.sources,
            request.method
        )

        logger.info { "Saving submission ${submission.accNo}" }
        return context.saveAndProcessSubmissionRequest(SaveRequest(submission, request.mode))
    }

    fun processRequest(request: SaveRequest): ExtSubmission {
        logger.info { "processing request for submission ${request.submission.accNo} " }
        return context.processSubmission(request)
    }

    fun submitAsync(request: SubmissionRequest): SaveRequest {
        logger.info { "processing async request $request" }

        val submission = process(
            request.submission,
            request.submitter.asUser(),
            request.onBehalfUser?.asUser(),
            request.sources,
            request.method
        )

        logger.info { "Saving submission request ${submission.accNo}" }
        return SaveRequest(context.saveSubmissionRequest(SaveRequest(submission, request.mode)), request.mode)
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
            return processSubmission(submission, submitter, onBehalfUser, source, method)
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
        val (parentTags, parentReleaseTime, parentPattern) = parentInfoService.getParentInfo(submission.attachTo)
        val (createTime, modTime, releaseTime) = getTimes(submission, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val accNo = getAccNumber(submission, submitter, parentPattern)
        val accNoString = accNo.toString()
        val projectInfo = getProjectInfo(submitter, submission, accNoString)
        val secretKey = getSecret(accNoString)
        val relPath = accNoService.getRelPath(accNo)
        val tags = getTags(parentTags, projectInfo)
        val ownerEmail = onBehalfUser?.email ?: queryService.getOwner(accNoString) ?: submitter.email

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
            projects = tags.map { Project(it) },
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

    private fun getTags(parentTags: List<String>, project: ProjectResponse?): List<String> {
        val tags = parentTags.filter { it != PUBLIC_ACCESS_TAG.value }.toMutableList()
        if (project != null) tags.add(project.accessTag)
        return tags
    }

    private fun getProjectInfo(user: User, submission: Submission, accNo: String) =
        projectInfoService.process(ProjectRequest(user.email, submission.section.type, submission.accNoTemplate, accNo))

    private fun getAttributes(submission: Submission) = submission.attributes
        .filterNot { RESERVED_ATTRIBUTES.contains(it.name) }
        .map { it.toExtAttribute() }

    private fun getAccNumber(sub: Submission, user: User, parentPattern: String?) =
        accNoService.getAccNo(AccNoServiceRequest(user.email, sub.accNo.ifBlank { null }, sub.attachTo, parentPattern))

    private fun getTimes(sub: Submission, parentReleaseTime: OffsetDateTime?) =
        timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, parentReleaseTime))

    private fun getSecret(accString: String) = queryService.getSecret(accString) ?: UUID.randomUUID().toString()
}
