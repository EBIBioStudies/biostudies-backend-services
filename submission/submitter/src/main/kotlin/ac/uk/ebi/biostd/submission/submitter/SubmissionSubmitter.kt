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
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
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
        val submission = ExtendedSubmission(request.submission, request.user.asUser())
        val processingErrors = process(submission, request.files)

        if (processingErrors.isEmpty()) {
            context.deleteSubmissionDrafts(submission)
            submission.processingStatus = ProcessingStatus.PROCESSED
            submission.method = request.method
            return context.saveSubmission(submission)
        }

        throw InvalidSubmissionException("Submission validation errors", processingErrors)
    }

    private fun process(submission: ExtendedSubmission, source: FilesSource) =
        listOf(
            runCatching { processSubmission(submission) },
            runCatching { filesHandler.processFiles(submission, source) }
        ).mapNotNull { it.exceptionOrNull() }

    private fun processSubmission(sub: ExtendedSubmission): ExtendedSubmission {
        val (parentTags, parentReleaseTime, parentPattern) = parentInfoService.getParentInfo(sub.attachTo)
        val (creationTime, modificationTime, releaseTime) =
            timesService.getTimes(TimesRequest(sub.accNo, sub.releaseDate, parentReleaseTime))
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val tags = if (released) parentTags + SubFields.PUBLIC_ACCESS_TAG.value else parentTags

        // TODO move to line 65 when accNoService does not depends of submission state
        sub.accessTags = tags.toMutableList()

        val accNo = accNoService.getAccNo(
            AccNoServiceRequest(sub.user, sub.accNo, sub.accessTags, sub.attachTo, parentPattern))
        val accString = accNo.toString()
        val relPath = accNoService.getRelPath(accNo)

        // TODO: move to accNoService and renamed class to make sense ot if and RelPath
        val secretKey = getSecret(accString)

        sub.version = context.getNextVersion(accString)
        sub.accNo = accString
        sub.relPath = relPath
        sub.accessTags = tags.toMutableList()
        sub.releaseDate = null
        sub.creationTime = creationTime
        sub.modificationTime = modificationTime
        sub.releaseTime = releaseTime
        sub.released = released
        sub.secretKey = secretKey
        return sub
    }

    private fun getSecret(accString: String) =
        if (context.isNew(accString)) UUID.randomUUID().toString() else context.getSecret(accString)
}
