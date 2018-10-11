package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ebi.ac.uk.model.ISubmission

/**
 * Add all parent submission tags to submitted submission.
 */
class AccessTagProcessor {

    fun processSubmission(submission: ISubmission, context: PersistenceContext) {
        submission.accessTags.addAll(context.getParentAccessTags(submission))
    }
}
