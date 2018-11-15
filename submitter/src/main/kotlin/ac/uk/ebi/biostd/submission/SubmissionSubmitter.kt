package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionSubmitter(
    private val processors: List<SubmissionProcessor>,
    private val filesHandler: FilesHandler
) {

    fun submit(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        processors.forEach { processor -> processor.process(submission, persistenceContext) }
        filesHandler.processFiles(submission)
        persistenceContext.saveSubmission(submission)
    }
}
