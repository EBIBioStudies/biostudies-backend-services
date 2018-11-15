package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User
import ebi.ac.uk.persistence.PersistenceContext

interface SubmissionProcessor {
    fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext)
}
