package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import org.bson.types.ObjectId

typealias EitherList<A, B> = List<Either<A, B>>

class ToDocSectionMapper(
    private val toDocFileListMapper: ToDocFileListMapper,
    private val toDocLinkListMapper: ToDocLinkListMapper,
) {
    internal fun convert(
        section: ExtSection,
        accNo: String,
        version: Int,
        subId: ObjectId,
    ): DocSection {
        val sections = section.sections.map { it.toDocSections(accNo, version, subId) }
        val sectionFileList = section.fileList?.let { toDocFileListMapper.convert(it) }
        val sectionLinkList = section.linkList?.let { toDocLinkListMapper.convert(it) }

        return section.convert(sectionFileList, sectionLinkList, sections)
    }

    private fun ExtSection.convert(
        fileList: DocFileList?,
        linkList: DocLinkList?,
        sections: EitherList<DocSection, DocSectionTable>,
    ) = DocSection(
        id = ObjectId(),
        accNo = accNo,
        type = type,
        fileList = fileList,
        linkList = linkList,
        attributes = attributes.map { it.toDocAttribute() },
        files = files.map { it.toDocFiles() },
        links = links.map { it.toDocLinks() },
        sections = sections,
    )

    private fun ExtSection.toDocTableSection() = DocSectionTableRow(accNo, type, attributes.map { it.toDocAttribute() })

    private fun ExtSectionTable.toDocSectionTable() = DocSectionTable(sections.map { it.toDocTableSection() })

    private fun Either<ExtSection, ExtSectionTable>.toDocSections(
        accNo: String,
        version: Int,
        submissionId: ObjectId,
    ): Either<DocSection, DocSectionTable> = bimap({ convert(it, accNo, version, submissionId) }) { it.toDocSectionTable() }
}
