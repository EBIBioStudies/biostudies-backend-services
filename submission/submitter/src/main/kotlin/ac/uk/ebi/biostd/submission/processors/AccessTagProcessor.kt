package ac.uk.ebi.biostd.submission.processors

import arrow.core.Try
import arrow.core.getOrElse
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

/**
 * Add all parent submission tags to submitted submission.
 */
class AccessTagProcessor : SubmissionProcessor {
    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        val accessTags = Try {
            context.getParentAccessTags(submission).filterNot { it == "Public" }
        }.getOrElse { emptyList() }

        submission.accessTags.addAll(accessTags)
    }
}
