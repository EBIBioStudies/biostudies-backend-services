package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.User

/**
 * Add all parent submission tags to submitted submission.
 */
class AccessTagProcessor : SubmissionProcessor {

    override fun process(user: User, submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        submission.accessTags.addAll(persistenceContext.getParentAccessTags(submission))
    }
}
