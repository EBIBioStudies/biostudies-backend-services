package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILES
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class DocFileListConverter(private val docFileConverter: DocFileConverter) : Converter<Document, DocFileList> {
    override fun convert(source: Document): DocFileList = DocFileList(
        fileName = source.getString(FILE_LIST_DOC_FILE_LIST),
        files = source.getDocList(FILE_LIST_DOC_FILES).map { docFileConverter.convert(it) }
    )
}
