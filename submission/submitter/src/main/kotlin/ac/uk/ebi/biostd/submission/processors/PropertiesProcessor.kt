package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext
import java.util.UUID

/**
 * Calculate and set general purpose properties values as submission title and submission secret.
 */
class PropertiesProcessor : SubmissionProcessor {
    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        if (context.isNew(submission)) {
            submission.secretKey = UUID.randomUUID().toString()
            submission.version = 1
        }
    }
}
