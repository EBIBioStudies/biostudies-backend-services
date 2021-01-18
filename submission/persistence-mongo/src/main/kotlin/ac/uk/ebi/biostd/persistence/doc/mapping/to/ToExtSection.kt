package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FilesSource

internal fun DocSection.toExtSection(filesSource: FilesSource): ExtSection = ExtSection(
    accNo = accNo,
    type = type,
    fileList = fileList?.toExtFileList(filesSource),
    attributes = attributes.map { it.toExtAttribute() },
    sections = sections.map { it.toExtSections(filesSource) },
    files = files.map { it.toExtFiles(filesSource) },
    links = links.map { it.toExtLinks() }
)

internal fun DocSectionTable.toExtSectionTable(
    filesSource: FilesSource
): ExtSectionTable = ExtSectionTable(sections.map { it.toExtSection(filesSource) })

internal fun Either<DocSection, DocSectionTable>.toExtSections(
    filesSource: FilesSource
): Either<ExtSection, ExtSectionTable> = bimap({ it.toExtSection(filesSource) }, { it.toExtSectionTable(filesSource) })
