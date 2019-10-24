package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.validators.SubmissionValidator
import arrow.core.Try
import arrow.core.getOrElse
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.collections.ifNotEmpty
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class SubmissionSubmitter(
    private val validators: List<SubmissionValidator>,
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(
        submission: ExtendedSubmission,
        files: FilesSource,
        context: PersistenceContext
    ): Submission {
        val exceptionList = mutableListOf<Throwable>()

        validators.map { Try { it.validate(submission, context) }.getOrElse { exceptionList.add(it) } }
        processors.map { Try { it.process(submission, context) }.getOrElse { exceptionList.add(it) } }
        Try { filesHandler.processFiles(submission, files) }.getOrElse { exceptionList.add(it) }

        exceptionList.ifNotEmpty {
            throw InvalidSubmissionException("Submission validation errors", exceptionList)
        }

        context.saveSubmission(submission)
        context.deleteSubmissionDrafts(submission)
        return submission
    }
}
