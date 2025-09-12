package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionFile
import ac.uk.ebi.biostd.persistence.doc.model.subFileDocClass
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class SubmissionFileConverter(
    private val fileConverter: FileConverter,
) : Converter<DocSubmissionFile, Document> {
    override fun convert(docSubmissionFile: DocSubmissionFile): Document {
        val docSubFile = Document()
        docSubFile[CommonsConverter.CLASS_FIELD] = subFileDocClass
        docSubFile[DOC_SUB_FILE_ID] = docSubmissionFile.id
        docSubFile[DOC_SUB_FILE_SUBMISSION_ACC_NO] = docSubmissionFile.submissionAccNo
        docSubFile[DOC_SUB_FILE_SUBMISSION_VERSION] = docSubmissionFile.submissionVersion
        docSubFile[DOC_SUB_FILE_FILE] = fileConverter.convert(docSubmissionFile.file)
        return docSubFile
    }
}
