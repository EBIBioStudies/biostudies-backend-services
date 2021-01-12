package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileListConverter(private val fileConverter: FileConverter) : Converter<DocFileList, Document> {
    override fun convert(docFileList: DocFileList): Document {
        val fileList = Document()
        fileList[classField] = clazz
        fileList[fileListDocFileName] = docFileList.fileName
        fileList[fileListDocFiles] = docFileList.files.map { fileConverter.convert(it) }
        return fileList
    }

    companion object {
        val clazz: String = DocFileList::class.java.canonicalName
        const val fileListDocFileName = "fileName"
        const val fileListDocFiles = "files"
    }
}
