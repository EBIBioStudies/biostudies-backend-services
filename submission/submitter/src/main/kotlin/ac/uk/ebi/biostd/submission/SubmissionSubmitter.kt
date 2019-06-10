package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.model.UserSource
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ac.uk.ebi.biostd.submission.validators.SubmissionValidator
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionSubmitter(
    private val validators: List<SubmissionValidator>,
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {
    fun submit(
        submission: ExtendedSubmission,
        files: UserSource,
        context: PersistenceContext
    ): Submission {
        validators.forEach { validator -> validator.validate(submission, context) }
        processors.forEach { processor -> processor.process(submission, context) }
        filesHandler.processFiles(submission, files)
        context.saveSubmission(submission)
        return submission
    }
}
