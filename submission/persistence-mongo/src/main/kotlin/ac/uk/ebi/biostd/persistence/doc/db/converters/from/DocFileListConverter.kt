package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocFileListConverter(private val docFileConverter: DocFileConverter) : Converter<Document, DocFileList> {
    override fun convert(source: Document): DocFileList {
        return DocFileList(
            fileName = source.getString(docFileListFileName),
            files = source.getDocList(docFileListFiles).map { docFileConverter.convert(it) }
        )
    }

    companion object {
        const val docFileListFileName = "fileName"
        const val docFileListFiles = "files"
    }
}
