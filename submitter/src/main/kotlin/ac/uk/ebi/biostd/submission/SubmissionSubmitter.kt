package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionSubmitter(
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {
    // /TODO: change processor logic specific order is needed for example is necesary to load validate attachTo first before accNo validation
    fun submit(submission: ExtendedSubmission, persistenceContext: PersistenceContext): Submission {
        processors.forEach { processor -> processor.process(submission, persistenceContext) }
        filesHandler.processFiles(submission)
        persistenceContext.saveSubmission(submission)
        return submission
    }
}
