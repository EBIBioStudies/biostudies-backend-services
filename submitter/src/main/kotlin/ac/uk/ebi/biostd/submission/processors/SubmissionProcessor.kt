package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User

interface SubmissionProcessor {
    fun process(user: User, submission: Submission, persistenceContext: PersistenceContext)
}
