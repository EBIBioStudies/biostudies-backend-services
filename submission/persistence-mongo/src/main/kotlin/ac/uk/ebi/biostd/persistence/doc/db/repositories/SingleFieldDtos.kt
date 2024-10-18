package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection

/**
 * Represent the project of a specific @see [ac.uk.ebi.biostd.persistence.doc.model.DocSubmission].
 */
data class SubmissionCollections(val collections: List<DocCollection>?)

data class MigrationData(val accNo: String, val version: Int)

data class ReleaseData(
    val accNo: String,
    val owner: String,
    val relPath: String,
)
