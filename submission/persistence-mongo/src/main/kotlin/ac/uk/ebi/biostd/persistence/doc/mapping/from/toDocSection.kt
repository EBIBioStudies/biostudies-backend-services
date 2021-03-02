package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.util.arrow.mapLeft
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2
import org.bson.types.ObjectId

internal fun ExtSection.toDocSection(submissionId: ObjectId): DocSectionDescriptor {
    val sections: List<Either<DocSectionDescriptor, DocSectionTable>> = sections.map { it.toDocSections(submissionId) }
    val (sectionFileList, sectionFiles) = fileList?.toDocFileList(submissionId)
    val docSection = toDocSection(sectionFileList, sections.map { either -> either.mapLeft { it.section } })
    return DocSectionDescriptor(docSection, sectionFiles.orEmpty() + sections.mapLeft { it.fileListFiles }.flatten())
}

private fun ExtSection.toDocSection(
    fileList: DocFileList?,
    sections: List<Either<DocSection, DocSectionTable>>
): DocSection = DocSection(
    accNo = accNo,
    type = type,
    fileList = fileList,
    attributes = attributes.map { it.toDocAttribute() },
    files = files.map { it.toDocFiles() },
    links = links.map { it.toDocLinks() },
    sections = sections
)

private fun ExtSection.toDocTableSection(): DocSectionTableRow = DocSectionTableRow(
    accNo = accNo,
    type = type,
    attributes = attributes.map { it.toDocAttribute() }
)
private fun Either<ExtSection, ExtSectionTable>.toDocSections(
    submissionId: ObjectId
): Either<DocSectionDescriptor, DocSectionTable> =
    bimap({ it.toDocSection(submissionId) }, ExtSectionTable::toDocSectionTable)

private fun ExtSectionTable.toDocSectionTable() =
    DocSectionTable(sections.map { it.toDocTableSection() })
