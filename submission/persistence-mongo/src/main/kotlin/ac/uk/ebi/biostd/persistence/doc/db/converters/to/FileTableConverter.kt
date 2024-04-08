package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileTableFields.DOC_FILE_TABLE_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileTableFields.FILE_TABLE_DOC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.CLASS_FIELD
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileTableConverter(private val fileConverter: FileConverter) : Converter<DocFileTable, Document> {
    override fun convert(docFileTable: DocFileTable): Document {
        val fileTable = Document()
        fileTable[CLASS_FIELD] = DOC_FILE_TABLE_CLASS
        fileTable[FILE_TABLE_DOC_FILES] = docFileTable.files.map { fileConverter.convert(it) }
        return fileTable
    }
}
