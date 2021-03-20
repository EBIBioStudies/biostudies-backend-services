package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable

internal fun DocSection.toExtSection(): ExtSection = ExtSection(
    accNo = accNo,
    type = type,
    fileList = fileList?.toExtFileList(),
    attributes = attributes.map { it.toExtAttribute() },
    sections = sections.map { it.toExtSections() },
    files = files.map { it.toExtFiles() },
    links = links.map { it.toExtLinks() }
)

internal fun DocSectionTableRow.toExtSection(): ExtSection = ExtSection(
    accNo = accNo,
    type = type,
    attributes = attributes.map { it.toExtAttribute() }
)

internal fun DocSectionTable.toExtSectionTable(): ExtSectionTable = ExtSectionTable(sections.map { it.toExtSection() })

internal fun Either<DocSection, DocSectionTable>.toExtSections(): Either<ExtSection, ExtSectionTable> =
    bimap({ it.toExtSection() }, { it.toExtSectionTable() })
