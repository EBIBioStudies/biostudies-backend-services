package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission

interface SubmissionProcessor {
    fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext)
}
