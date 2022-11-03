package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import org.bson.types.ObjectId

/**
 * Represent the project of a specific @see [ac.uk.ebi.biostd.persistence.doc.model.DocSubmission].
 */
data class SubmissionCollections(val collections: List<DocCollection>?)

data class SubmissionRelPath(val _id: ObjectId, val relPath: String)
