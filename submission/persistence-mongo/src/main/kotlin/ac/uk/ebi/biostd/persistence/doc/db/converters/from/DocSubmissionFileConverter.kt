package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFileFields.DOC_SUB_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionFile
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocSubmissionFileConverter(
    private val docFileConvert: DocFileConverter,
) : Converter<Document, DocSubmissionFile> {
    override fun convert(source: Document): DocSubmissionFile =
        DocSubmissionFile(
            id = source.getObjectId(DOC_SUB_FILE_ID),
            file = docFileConvert.convert(source.get(DOC_SUB_FILE_FILE, Document::class.java)),
            submissionAccNo = source.getString(DOC_SUB_FILE_SUBMISSION_ACC_NO),
            submissionVersion = source.getInteger(DOC_SUB_FILE_SUBMISSION_VERSION),
        )
}
