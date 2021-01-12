package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
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
        val sectionDoc = basicSection(section)
        sectionDoc[secFileList] = section.fileList?.let { fileListConverter.convert(it) }
        sectionDoc[secSections] = getSections(section.sections)
        sectionDoc[secFiles] = getFiles(section.files)
        sectionDoc[secLinks] = getLinks(section.links)
        return sectionDoc
    }

    private fun basicSection(section: DocSection): Document {
        val sectionDoc = Document()
        sectionDoc[classField] = sectionClass
        sectionDoc[secAccNo] = section.accNo
        sectionDoc[secType] = section.type
        sectionDoc[secAttributes] = section.attributes.map { attributeConverter.convert(it) }
        return sectionDoc
    }

    private fun sectionsTable(docSectionTable: DocSectionTable): Document {
        val sectionTableDocument = Document()
        sectionTableDocument[classField] = sectionTableClass
        sectionTableDocument[secTableSections] = docSectionTable.sections.map { basicSection(it) }
        return sectionTableDocument
    }

    private fun getSections(sections: List<Either<DocSection, DocSectionTable>>): List<Document> =
        sections.map { either -> either.fold({ convert(it) }, { sectionsTable(it) }) }

    private fun getLinks(links: List<Either<DocLink, DocLinkTable>>): List<Document> =
        links.map { either -> either.fold({ linkConverter.convert(it) }, { linkTableConverter.convert(it) }) }

    private fun getFiles(files: List<Either<DocFile, DocFileTable>>): List<Document> =
        files.map { either -> either.fold({ fileConverter.convert(it) }, { fileTableConverter.convert(it) }) }

    companion object {
        val sectionClass: String = DocSection::class.java.canonicalName
        val sectionTableClass: String = DocSectionTable::class.java.canonicalName
        const val secAccNo = "accNo"
        const val secType = "type"
        const val secFileList = "fileList"
        const val secSections = "sections"
        const val secFiles = "files"
        const val secLinks = "links"
        const val secAttributes = "attributes"
        const val secTableSections = "sections"
    }
}
