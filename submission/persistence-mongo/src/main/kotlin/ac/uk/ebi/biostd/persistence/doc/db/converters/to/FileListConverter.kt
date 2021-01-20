package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.DOC_FILE_LIST_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileListConverter(private val fileConverter: FileConverter) : Converter<DocFileList, Document> {
    override fun convert(docFileList: DocFileList): Document {
        val fileList = Document()
        fileList[classField] = DOC_FILE_LIST_CLASS
        fileList[FILE_LIST_DOC_FILE_LIST] = docFileList.fileName
        fileList[FILE_LIST_DOC_FILES] = docFileList.files.map { fileConverter.convert(it) }
        return fileList
    }
}
