package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission

/**
 * Add all parent submission tags to submitted submission.
 */
class AccessTagProcessor : SubmissionProcessor {

    override fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        submission.accessTags.addAll(persistenceContext.getParentAccessTags(submission))
    }
}
