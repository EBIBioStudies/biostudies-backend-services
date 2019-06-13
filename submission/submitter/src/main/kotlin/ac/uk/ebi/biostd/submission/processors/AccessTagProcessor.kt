package ac.uk.ebi.biostd.submission.processors

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext

/**
 * Add all parent submission tags to submitted submission.
 */
class AccessTagProcessor : SubmissionProcessor {
    override fun process(submission: ExtendedSubmission, context: PersistenceContext) {
        val accessTags =
            context.getParentAccessTags(submission)
                .toMutableList()
                .apply { removeIf { it == "Public" && submission.releaseDate.isNullOrBlank().not() } }

        submission.accessTags.addAll(accessTags)
    }
}
