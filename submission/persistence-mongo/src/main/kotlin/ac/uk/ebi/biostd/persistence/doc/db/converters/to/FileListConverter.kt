package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.DOC_FILE_LIST_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILE_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileListFields.FILE_LIST_DOC_PAGE_TAB_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import org.bson.Document

class FileListConverter(private val fileRefConverter: FileRefConverter, private val fileConverter: FileConverter) {

    fun convert(docFileList: DocFileList): Document {
        val fileList = Document()
        fileList[classField] = DOC_FILE_LIST_CLASS
        fileList[FILE_LIST_DOC_FILE_FILENAME] = docFileList.fileName
        fileList[FILE_LIST_DOC_FILES] = docFileList.files.map { fileRefConverter.convert(it) }
        fileList[FILE_LIST_DOC_PAGE_TAB_FILES] = docFileList.pageTabFiles.map { fileConverter.convert(it) }
        return fileList
    }
}
