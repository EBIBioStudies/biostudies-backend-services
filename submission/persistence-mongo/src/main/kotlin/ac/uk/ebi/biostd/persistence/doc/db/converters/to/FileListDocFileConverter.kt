package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.Document

class FileListDocFileConverter {
    fun convert(fileListDocFile: FileListDocFile): Document {
        val fileListDocFileDoc = Document()
        fileListDocFileDoc[CommonsConverter.classField] = FileListDocFileFields.FILE_LIST_DOC_FILE_CLASS
        fileListDocFileDoc[FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID] = fileListDocFile.submissionId
        fileListDocFileDoc[FileListDocFileFields.FILE_LIST_DOC_FILE_FILENAME] = fileListDocFile.fileName
        fileListDocFileDoc[FileListDocFileFields.FILE_LIST_DOC_FILE_FULL_PATH] = fileListDocFile.fullPath
        fileListDocFileDoc[FileListDocFileFields.FILE_LIST_DOC_FILE_ATTRIBUTES] = fileListDocFile.attributes
        fileListDocFileDoc[FileListDocFileFields.FILE_LIST_DOC_FILE_MD5] = fileListDocFile.md5
        return fileListDocFileDoc
    }
}
