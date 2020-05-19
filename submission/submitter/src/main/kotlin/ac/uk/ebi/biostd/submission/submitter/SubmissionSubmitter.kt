package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SaveRequest
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.events.SuccessfulSubmission
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
import ebi.ac.uk.base.ifTrue
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.serialization.from.toExtAttribute
import ebi.ac.uk.extended.mapping.serialization.from.toExtSection
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
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
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID
import ac.uk.ebi.biostd.submission.events.SubmissionEvents.successfulSubmission as submitEvent

open class SubmissionSubmitter(
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val parentInfoService: ParentInfoService,
    private val projectInfoService: ProjectInfoService,
    private val context: PersistenceContext,
    private val queryService: SubmissionQueryService
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(request: SubmissionRequest): Submission {
        val submitter = request.submitter.asUser()
        val submission = process(
            request.submission,
            request.submitter.asUser(),
            request.onBehalfUser?.asUser(),
            request.sources,
            request.method
        )
        val submitted = context.saveSubmission(SaveRequest(submission, request.mode)).toSimpleSubmission()
        submitter.notificationsEnabled.ifTrue { submitEvent.onNext(SuccessfulSubmission(submitter, submission)) }
        return submitted
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
    ):
        ExtSubmission {
        val (parentTags, parentReleaseTime, parentPattern) = parentInfoService.getParentInfo(submission.attachTo)
        val (createTime, modTime, releaseTime) = getTimes(submission, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val accNo = getAccNumber(submission, submitter, parentPattern)
        val accNoString = accNo.toString()
        val projectInfo = getProjectInfo(submitter, submission, accNoString)
        val secretKey = getSecret(accNoString)
        val nextVersion = context.getNextVersion(accNoString)
        val relPath = accNoService.getRelPath(accNo)
        val tags = getTags(released, parentTags, projectInfo)
        val ownerEmail = onBehalfUser?.email ?: queryService.getOwner(accNoString) ?: submitter.email

        return ExtSubmission(
            accNo = accNoString,
            owner = ownerEmail,
            submitter = submitter.email,
            version = nextVersion,
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
            accessTags = tags.map { ExtAccessTag(it) },
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

    private fun getTags(released: Boolean, parentTags: List<String>, project: ProjectResponse?): List<String> {
        val tags = parentTags.toMutableList()
        if (released) tags.add(PUBLIC_ACCESS_TAG.value)
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

    private fun getSecret(accString: String) =
        if (queryService.isNew(accString)) UUID.randomUUID().toString() else queryService.getSecret(accString)
}
