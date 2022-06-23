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
import ebi.ac.uk.util.collections.reduceLeft
import org.bson.types.ObjectId

typealias EitherList <A, B> = List<Either<A, B>>

class ToDocSectionMapper(private val toDocFileListMapper: ToDocFileListMapper) {
    internal fun convert(section: ExtSection, accNo: String, version: Int, subId: ObjectId): DocSectionData {
        val sections = section.sections.map { it.toDocSections(accNo, version, subId) }
        val (sectionFileList, sectionFiles) = section.fileList?.let {
            toDocFileListMapper.convert(it, subId, accNo, version)
        }
        return DocSectionData(
            section = section.convert(sectionFileList, sections.subSections()),
            fileListFiles = sectionFiles.orEmpty() + sections.subSectionsFiles()
        )
    }

    private fun EitherList<DocSectionData, DocSectionTable>.subSections(): EitherList<DocSection, DocSectionTable> =
        map { either -> either.mapLeft { it.section } }

    private fun EitherList<DocSectionData, DocSectionTable>.subSectionsFiles(): List<FileListDocFile> =
        reduceLeft { it.fileListFiles }.flatten()

    private fun ExtSection.convert(
        fileList: DocFileList?,
        sections: EitherList<DocSection, DocSectionTable>,
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

    private fun Either<ExtSection, ExtSectionTable>.toDocSections(
        accNo: String,
        version: Int,
        submissionId: ObjectId,
    ): Either<DocSectionData, DocSectionTable> {
        return bimap({ convert(it, accNo, version, submissionId) }) { it.toDocSectionTable() }
    }
}
