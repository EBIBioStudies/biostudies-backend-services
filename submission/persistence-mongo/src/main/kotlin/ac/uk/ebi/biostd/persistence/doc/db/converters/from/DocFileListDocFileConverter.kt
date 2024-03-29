package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocFileListDocFileConverter(private val docFileConvert: DocFileConverter) : Converter<Document, FileListDocFile> {
    override fun convert(source: Document): FileListDocFile {
        return FileListDocFile(
            id = source.getObjectId(FILE_LIST_DOC_FILE_ID),
            submissionId = source.getObjectId(FILE_LIST_DOC_FILE_SUBMISSION_ID),
            file = docFileConvert.convert(source.get(FILE_LIST_DOC_FILE_FILE, Document::class.java)),
            fileListName = source.getString(FILE_LIST_DOC_FILE_FILE_LIST_NAME),
            index = source.getInteger(FILE_LIST_DOC_FILE_INDEX),
            submissionAccNo = source.getString(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO),
            submissionVersion = source.getInteger(FILE_LIST_DOC_FILE_SUBMISSION_VERSION),
        )
    }
}
