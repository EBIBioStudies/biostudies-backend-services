package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.DOC_SEC_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.DOC_SEC_TABLE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.DOC_TABLE_SEC_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_LINKS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_SECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TABLE_SECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import arrow.core.Either
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class SectionConverter(
    private val attributeConverter: AttributeConverter,
    private val linkConverter: LinkConverter,
    private val linkTableConverter: LinkTableConverter,
    private val fileConverter: FileConverter,
    private val fileTableConverter: FileTableConverter,
    private val fileListConverter: FileListConverter
) : Converter<DocSection, Document> {

    override fun convert(section: DocSection): Document {
        val sectionDoc = Document()
        sectionDoc[classField] = DOC_SEC_CLASS
        sectionDoc[SEC_ACC_NO] = section.accNo
        sectionDoc[SEC_TYPE] = section.type
        sectionDoc[SEC_ATTRIBUTES] = section.attributes.map { attributeConverter.convert(it) }
        sectionDoc[SEC_FILE_LIST] = section.fileList?.let { fileListConverter.convert(it) }
        sectionDoc[SEC_SECTIONS] = getSections(section.sections)
        sectionDoc[SEC_FILES] = getFiles(section.files)
        sectionDoc[SEC_LINKS] = getLinks(section.links)
        return sectionDoc
    }

    private fun tableSection(section: DocSectionTableRow): Document {
        val sectionDoc = Document()
        sectionDoc[classField] = DOC_TABLE_SEC_CLASS
        sectionDoc[SEC_ACC_NO] = section.accNo
        sectionDoc[SEC_TYPE] = section.type
        sectionDoc[SEC_ATTRIBUTES] = section.attributes.map { attributeConverter.convert(it) }
        return sectionDoc
    }

    private fun sectionsTable(docSectionTable: DocSectionTable): Document {
        val sectionTableDocument = Document()
        sectionTableDocument[classField] = DOC_SEC_TABLE_CLASS
        sectionTableDocument[SEC_TABLE_SECTIONS] = docSectionTable.sections.map { tableSection(it) }
        return sectionTableDocument
    }

    private fun getSections(sections: List<Either<DocSection, DocSectionTable>>): List<Document> =
        sections.map { either -> either.fold({ convert(it) }, { sectionsTable(it) }) }

    private fun getLinks(links: List<Either<DocLink, DocLinkTable>>): List<Document> =
        links.map { either -> either.fold({ linkConverter.convert(it) }, { linkTableConverter.convert(it) }) }

    private fun getFiles(files: List<Either<DocFile, DocFileTable>>): List<Document> =
        files.map { either -> either.fold({ fileConverter.convert(it) }, { fileTableConverter.convert(it) }) }
}
