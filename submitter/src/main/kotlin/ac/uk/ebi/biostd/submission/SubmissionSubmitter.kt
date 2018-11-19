package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
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
    fun submit(submission: ExtendedSubmission, persistenceContext: PersistenceContext): Submission {
        validators.forEach { validator -> validator.validate(submission, persistenceContext) }
        processors.forEach { processor -> processor.process(submission, persistenceContext) }
        filesHandler.processFiles(submission)
        persistenceContext.saveSubmission(submission)
        return submission
    }
}
