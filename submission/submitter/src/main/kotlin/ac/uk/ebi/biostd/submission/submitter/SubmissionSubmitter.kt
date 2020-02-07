package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesRequest
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.serialization.from.toExtAttribute
import ebi.ac.uk.extended.mapping.serialization.from.toExtSection
import ebi.ac.uk.extended.mapping.serialization.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.util.date.isBeforeOrEqual
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

open class SubmissionSubmitter(
    private val filesHandler: FilesHandler,
    private val timesService: TimesService,
    private val accNoService: AccNoService,
    private val parentInfoService: ParentInfoService,
    private val context: PersistenceContext
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(request: SubmissionRequest): Submission {
        val user = request.user.asUser()
        val submission = ExtendedSubmission(request.submission, user)
        val extSubmission = process(request.submission, submission, request.files, request.method)

        context.deleteSubmissionDrafts(user.id, extSubmission.accNo)
        return context.saveSubmission(extSubmission, user.email).toSimpleSubmission()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun process(
        submission: Submission,
        extSubmission: ExtendedSubmission,
        source: FilesSource,
        method: SubmissionMethod
    ): ExtSubmission {
        try {
            val ext = processSubmission(submission, extSubmission.user, source, method)
            filesHandler.processFiles(extSubmission, source)
            return ext
        } catch (e: RuntimeException) {
            throw InvalidSubmissionException("Submission validation errors", listOf(e))
        }
    }

    private fun processSubmission(submission: Submission, user: User, source: FilesSource, method: SubmissionMethod):
        ExtSubmission {
        val (parentTags, parentReleaseTime, parentPattern) = parentInfoService.getParentInfo(submission.attachTo)
        val (createTime, modTime, releaseTime) = getTimes(submission, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val tags = if (released) parentTags + SubFields.PUBLIC_ACCESS_TAG.value else parentTags
        val accNo = getAccNumber(submission, user, parentPattern)
        val accNoString = accNo.toString()
        val secretKey = getSecret(accNoString)
        val nextVersion = context.getNextVersion(accNoString)
        val relPath = accNoService.getRelPath(accNo)

        return ExtSubmission(
            accNo = accNoString,
            version = nextVersion,
            method = method,
            title = null,
            relPath = relPath,
            rootPath = submission.rootPath,
            released = released,
            secretKey = secretKey,
            status = ProcessingStatus.PROCESSED,
            releaseTime = releaseTime,
            modificationTime = modTime,
            creationTime = createTime,
            tags = submission.tags.map { ExtTag(it.first, it.second) },
            accessTags = tags.map { ExtAccessTag(it) },
            section = submission.section.toExtSection(source),
            attributes = submission.attributes
                .filter { it.name !=  SubFields.RELEASE_DATE.value }
                .map { it.toExtAttribute() }
        )
    }

    private fun getAccNumber(sub: Submission, user: User, parentPattern: String?) =
        accNoService.getAccNo(AccNoServiceRequest(user, sub.accNo, sub.accessTags, sub.attachTo, parentPattern))

    private fun getTimes(sub: Submission, parentReleaseTime: OffsetDateTime?) =
        timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, parentReleaseTime))

    private fun getSecret(accString: String) =
        if (context.isNew(accString)) UUID.randomUUID().toString() else context.getSecret(accString)
}
