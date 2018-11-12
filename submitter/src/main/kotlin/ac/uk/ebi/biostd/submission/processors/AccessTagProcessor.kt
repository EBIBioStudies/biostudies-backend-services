package ac.uk.ebi.biostd.submission.helpers

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.submission.process.SubmissionProcessor
import ebi.ac.uk.model.Submission

/**
 * Add all parent submission tags to submitted submission.
 */
class AccessTagProcessor : SubmissionProcessor {

    
    fun processSubmission(submission: Submission, context: PersistenceContext) {
        submission.accessTags.addAll(context.getParentAccessTags(submission))
    }
}
