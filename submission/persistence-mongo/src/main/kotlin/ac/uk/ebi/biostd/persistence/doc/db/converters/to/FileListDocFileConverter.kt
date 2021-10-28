package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class FileListDocFileConverter(private val fileConverter: FileConverter): Converter<FileListDocFile, Document> {
    override fun convert(fileListDocFile: FileListDocFile): Document {
        val fileListDocFileDoc = Document()
        FileListDocFileFields.FILE_LIST_DOC_FILE_CLASS.also { fileListDocFileDoc[CommonsConverter.classField] = it }
        fileListDocFileDoc[FILE_LIST_DOC_FILE_SUBMISSION_ID] = fileListDocFile.submissionId
        fileListDocFileDoc[FILE_LIST_DOC_FILE_FILE] = fileConverter.convert(fileListDocFile.file)

        return fileListDocFileDoc
    }
}
