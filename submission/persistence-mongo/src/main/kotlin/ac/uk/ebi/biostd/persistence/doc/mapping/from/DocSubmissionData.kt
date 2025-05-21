package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.LinkListDocLink

data class DocSubmissionData(
    val section: DocSubmission,
    val fileListFiles: List<FileListDocFile>,
    val linkListLinks: List<LinkListDocLink>,
)
