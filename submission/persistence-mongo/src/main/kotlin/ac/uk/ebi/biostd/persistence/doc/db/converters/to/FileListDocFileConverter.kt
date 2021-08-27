package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILENAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_MD5
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.Document

class FileListDocFileConverter {
    fun convert(fileListDocFile: FileListDocFile): Document {
        val fileListDocFileDoc = Document()
        FileListDocFileFields.FILE_LIST_DOC_FILE_CLASS.also { fileListDocFileDoc[CommonsConverter.classField] = it }
        fileListDocFileDoc[FILE_LIST_DOC_FILE_SUBMISSION_ID] = fileListDocFile.submissionId
        fileListDocFileDoc[FILE_LIST_DOC_FILE_FILENAME] = fileListDocFile.fileName
        fileListDocFileDoc[FILE_LIST_DOC_FILE_FULL_PATH] = fileListDocFile.location
        fileListDocFileDoc[FILE_LIST_DOC_FILE_ATTRIBUTES] = fileListDocFile.attributes
        fileListDocFileDoc[FILE_LIST_DOC_FILE_MD5] = fileListDocFile.md5
        return fileListDocFileDoc
    }
}
