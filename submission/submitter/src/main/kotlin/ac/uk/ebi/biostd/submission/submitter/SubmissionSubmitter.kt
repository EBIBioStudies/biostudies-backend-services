package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.SubmissionRequest
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class SubmissionSubmitter(
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(request: SubmissionRequest, persistenceContext: PersistenceContext): Submission {
        val submission = ExtendedSubmission(request.submission, request.user.asUser())
        val processingErrors = process(submission, request.files, persistenceContext)

        if (processingErrors.isEmpty()) {
            persistenceContext.deleteSubmissionDrafts(submission)
            submission.processingStatus = PROCESSED
            submission.source = request.source
            return persistenceContext.saveSubmission(submission)
        }

        throw InvalidSubmissionException("Submission validation errors", processingErrors)
    }

    private fun process(submission: ExtendedSubmission, source: FilesSource, context: PersistenceContext) =
        processors
            .map { runCatching { it.process(submission, context) } }
            .plus(runCatching { filesHandler.processFiles(submission, source) })
            .mapNotNull { it.exceptionOrNull() }
}
