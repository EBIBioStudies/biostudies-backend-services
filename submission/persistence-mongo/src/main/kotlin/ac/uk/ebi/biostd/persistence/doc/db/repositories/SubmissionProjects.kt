package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection

/**
 * Represent the project of an specific @see [ac.uk.ebi.biostd.persistence.doc.model.DocSubmission].
 */
data class SubmissionProjects(val collections: List<DocCollection>)
