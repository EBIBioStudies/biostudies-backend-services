package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.service.AccNoService
import ac.uk.ebi.biostd.submission.service.AccNoServiceRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ac.uk.ebi.biostd.submission.service.TimesService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
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

    private fun processSubmission(submission: ExtendedSubmission): ExtendedSubmission {
        val (parentTags, parentReleaseTime) = parentInfoService.getParentInfo(submission.attachTo)
        val (creationTime, modificationTime, releaseTime) = timesService.getTimes(submission, parentReleaseTime)
        val released = releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()
        val tags = if (released) parentTags + SubFields.PUBLIC_ACCESS_TAG.value else parentTags

        // TODO move to line 65 when accNoService does not depends of submission state
        submission.accessTags = tags.toMutableList()

        val accNo = accNoService.getAccNo(
            AccNoServiceRequest(submission.user, submission.accNo, submission.accessTags, submission.attachTo))
        val accString = accNo.toString()
        val relPath = accNoService.getRelPath(accNo)

        // TODO: move to accNoService and renamed class to make sense ot if and RelPath
        val secretKey = getSecret(accString)

        submission.accNo = accString
        submission.relPath = relPath
        submission.accessTags = tags.toMutableList()
        submission.releaseDate = null
        submission.creationTime = creationTime
        submission.modificationTime = modificationTime
        submission.releaseTime = releaseTime
        submission.released = released
        submission.secretKey = secretKey
        return submission
    }

    private fun getSecret(accString: String) =
        if (context.isNew(accString)) UUID.randomUUID().toString() else context.getSecret(accString)
}
