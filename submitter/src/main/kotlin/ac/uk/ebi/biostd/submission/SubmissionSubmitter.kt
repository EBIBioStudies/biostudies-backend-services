package ac.uk.ebi.biostd.submission

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.submission.handlers.FilesHandler
import ac.uk.ebi.biostd.submission.processors.SubmissionProcessor
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

class SubmissionSubmitter(private val processors: List<SubmissionProcessor>, private val filesHandler: FilesHandler) {

    fun submit(user: User, submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        processors.forEach { processor -> processor.process(user, submission, persistenceContext) }
        filesHandler.processFiles(user, submission)
    }
}
