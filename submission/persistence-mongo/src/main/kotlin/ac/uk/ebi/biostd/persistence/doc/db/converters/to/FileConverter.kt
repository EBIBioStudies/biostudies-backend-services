package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileConverter(private val attributeConverter: AttributeConverter) : Converter<DocFile, Document> {
    override fun convert(docFile: DocFile): Document {
        val file = Document()
        file[classField] = clazz
        file[fileDocFilePath] = docFile.filePath
        file[fileDocAttributes] = docFile.attributes.map { attributeConverter.convert(it) }
        file[fileDocMd5] = docFile.md5
        return file
    }

    companion object {
        val clazz: String = DocFile::class.java.canonicalName
        const val fileDocFilePath = "filePath"
        const val fileDocAttributes = "attributes"
        const val fileDocMd5 = "md5"
    }
}
