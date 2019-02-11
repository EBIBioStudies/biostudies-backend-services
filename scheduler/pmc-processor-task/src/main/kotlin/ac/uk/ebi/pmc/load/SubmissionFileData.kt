package ac.uk.ebi.pmc.load

import java.time.Instant

/**
 * Contain submission file specification data.
 */
class SubmissionFileData(val name: String, val content: String, val modified: Instant)
