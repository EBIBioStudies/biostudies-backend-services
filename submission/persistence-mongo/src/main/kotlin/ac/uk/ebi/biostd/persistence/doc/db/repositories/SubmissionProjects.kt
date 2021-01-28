package ac.uk.ebi.biostd.persistence.doc.db.repositories

import ac.uk.ebi.biostd.persistence.doc.model.DocProject

/**
 * Represent the project of an specific @see [ac.uk.ebi.biostd.persistence.doc.model.DocSubmission].
 */
data class SubmissionProjects(val projects: List<DocProject>)
