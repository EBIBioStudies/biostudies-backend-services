package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.util.collections.component1
import ebi.ac.uk.util.collections.component2
import ebi.ac.uk.util.collections.mapLeft
import org.bson.types.ObjectId

typealias EitherList <A, B> = List<Either<A, B>>

internal fun ExtSection.toDocSection(submissionId: ObjectId): DocSectionData {
    val sections = sections.map { it.toDocSections(submissionId) }
    val (sectionFileList, sectionFiles) = fileList?.toDocFileList(submissionId)
    return DocSectionData(
        section = toDocSection(sectionFileList, sections.subSections()),
        fileListFiles = sectionFiles.orEmpty() + sections.subSectionsFiles()
    )
}

private fun EitherList<DocSectionData, DocSectionTable>.subSections(): EitherList<DocSection, DocSectionTable> =
    map { either -> either.mapLeft { it.section } }

private fun EitherList<DocSectionData, DocSectionTable>.subSectionsFiles(): List<FileListDocFile> =
    mapLeft { it.fileListFiles }.flatten()

private fun ExtSection.toDocSection(
    fileList: DocFileList?,
    sections: EitherList<DocSection, DocSectionTable>
) = DocSection(
    id = ObjectId(),
    accNo = accNo,
    type = type,
    fileList = fileList,
    attributes = attributes.map { it.toDocAttribute() },
    files = files.map { it.toDocFiles() },
    links = links.map { it.toDocLinks() },
    sections = sections
)

private fun ExtSection.toDocTableSection() = DocSectionTableRow(accNo, type, attributes.map { it.toDocAttribute() })
private fun ExtSectionTable.toDocSectionTable() = DocSectionTable(sections.map { it.toDocTableSection() })

private fun Either<ExtSection, ExtSectionTable>.toDocSections(submissionId: ObjectId) =
    bimap({ it.toDocSection(submissionId) }, ExtSectionTable::toDocSectionTable)
