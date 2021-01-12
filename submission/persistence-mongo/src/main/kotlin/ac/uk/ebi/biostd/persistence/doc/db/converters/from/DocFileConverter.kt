package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocFileConverter(private val docAttributeConverter: DocAttributeConverter) : Converter<Document, DocFile> {
    override fun convert(source: Document): DocFile {
        return DocFile(
            filePath = source.getString(docFileFilePath),
            attributes = source.getDocList(docFileAttributes).map { docAttributeConverter.convert(it) },
            md5 = source.getString(docFileMd5)
        )
    }

    companion object {
        const val docFileFilePath = "filePath"
        const val docFileAttributes = "attributes"
        const val docFileMd5 = "md5"
    }
}
