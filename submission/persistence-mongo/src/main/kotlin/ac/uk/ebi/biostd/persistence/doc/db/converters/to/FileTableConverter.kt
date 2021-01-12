package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileTableConverter(private val fileConverter: FileConverter) : Converter<DocFileTable, Document> {
    override fun convert(docFileTable: DocFileTable): Document {
        val fileTable = Document()
        fileTable[classField] = clazz
        fileTable[fileTableDocFiles] = docFileTable.files.map { fileConverter.convert(it) }
        return fileTable
    }

    companion object {
        val clazz: String = DocFileTable::class.java.canonicalName
        const val fileTableDocFiles = "files"
    }
}
