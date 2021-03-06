package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile

data class DocSectionData(val section: DocSection, val fileListFiles: List<FileListDocFile>)
