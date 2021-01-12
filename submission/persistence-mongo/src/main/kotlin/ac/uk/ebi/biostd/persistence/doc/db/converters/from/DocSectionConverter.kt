package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import arrow.core.Either
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocSectionConverter(
    private val docAttributeConverter: DocAttributeConverter,
    private val docLinkConverter: DocLinkConverter,
    private val docLinkTableConverter: DocLinkTableConverter,
    private val docFileConverter: DocFileConverter,
    private val docFileTableConverter: DocFileTableConverter,
    private val docFileListConverter: DocFileListConverter
) : Converter<Document, DocSection> {
    override fun convert(source: Document): DocSection {
        return DocSection(
            accNo = source.getString(secAccNo),
            type = source.getString(secType),
            fileList = source.findDoc(secFileList)?.let { docFileListConverter.convert(it) },
            attributes = source.getDocList(secAttributes).map { docAttributeConverter.convert(it) },
            sections = source.getDocList(secSections).map { toEitherSections(it) },
            files = source.getDocList(secFiles).map { toEitherFiles(it) },
            links = source.getDocList(secLinks).map { toEitherLinks(it) }
        )
    }

    private fun toEitherLinks(doc: Document): Either<DocLink, DocLinkTable> {
        return when (val clazz = doc.getString(classField)) {
            classDocLink -> Either.left(docLinkConverter.convert(doc))
            classDocLinkTable -> Either.right(docLinkTableConverter.convert(doc))
            else -> throw IllegalStateException("Expecting $clazz to be one of [$classDocLink , $classDocLinkTable]")
        }
    }

    private fun toEitherFiles(doc: Document): Either<DocFile, DocFileTable> {
        return when (val clazz = doc.getString(classField)) {
            classDocFile -> Either.left(docFileConverter.convert(doc))
            classDocFileTable -> Either.right(docFileTableConverter.convert(doc))
            else -> throw IllegalStateException("Expecting $clazz to be one of [$classDocFile , $classDocFileTable]")
        }
    }

    private fun toEitherSections(doc: Document): Either<DocSection, DocSectionTable> {
        return when (val clazz = doc.getString(classField)) {
            classDocSect -> Either.left(convert(doc))
            classDocSectTable -> Either.right(toSectionTable(doc))
            else -> throw IllegalStateException("Expecting $clazz to be one of [$classDocSect , $classDocSectTable]")
        }
    }

    private fun toSectionTable(document: Document): DocSectionTable =
        DocSectionTable(sections = document.getDocList(secTableSections).map { basicConvert(it) })

    private fun basicConvert(doc: Document): DocSection {
        return DocSection(
            accNo = doc.getString(secAccNo),
            type = doc.getString(secType),
            attributes = doc.getDocList(secAttributes).map { docAttributeConverter.convert(it) }
        )
    }

    companion object {
        val classDocLink: String = DocLink::class.java.canonicalName
        val classDocLinkTable: String = DocLinkTable::class.java.canonicalName
        val classDocFile: String = DocFile::class.java.canonicalName
        val classDocFileTable: String = DocFileTable::class.java.canonicalName
        val classDocSect: String = DocSection::class.java.canonicalName
        val classDocSectTable: String = DocSectionTable::class.java.canonicalName

        const val classField = "_class"
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
