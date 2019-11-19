package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.SubmissionErrorsContext
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class SubmissionSubmitter(
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(
        submission: ExtendedSubmission,
        files: FilesSource,
        persistenceContext: PersistenceContext
    ): Submission {
        val errorContext = SubmissionErrorsContext()

        processors.map { errorContext.runCatching { it.process(submission, persistenceContext) } }
        errorContext.runCatching { filesHandler.processFiles(submission, files) }
        errorContext.handleErrors()

        persistenceContext.deleteSubmissionDrafts(submission)

        return persistenceContext.saveSubmission(submission)
    }
}
