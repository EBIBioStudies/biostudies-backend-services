package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.fileListDocFileDocFileClass
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileListDocFileConverter(private val fileConverter: FileConverter) : Converter<FileListDocFile, Document> {
    override fun convert(fileListDocFile: FileListDocFile): Document {
        val fileListDocFileDoc = Document()
        fileListDocFileDoc[CommonsConverter.classField] = fileListDocFileDocFileClass
        fileListDocFileDoc[FILE_LIST_DOC_FILE_ID] = fileListDocFile.id
        fileListDocFileDoc[FILE_LIST_DOC_FILE_INDEX] = fileListDocFile.index
        fileListDocFileDoc[FILE_LIST_DOC_FILE_FILE_LIST] = fileListDocFile.fileListName
        fileListDocFileDoc[FILE_LIST_DOC_FILE_SUBMISSION_ID] = fileListDocFile.submissionId
        fileListDocFileDoc[FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO] = fileListDocFile.submissionAccNo
        fileListDocFileDoc[FILE_LIST_DOC_FILE_SUBMISSION_VERSION] = fileListDocFile.submissionVersion
        fileListDocFileDoc[FILE_LIST_DOC_FILE_FILE] = fileConverter.convert(fileListDocFile.file)
        return fileListDocFileDoc
    }
}
