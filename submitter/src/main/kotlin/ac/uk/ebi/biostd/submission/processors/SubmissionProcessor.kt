package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

interface SubmissionProcessor {
    fun process(user: User, submission: ExtendedSubmission, persistenceContext: PersistenceContext)
}
