package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.DOC_PROJECT_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.DOC_SUBMISSION_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.DOC_TAG_CLASS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.PAGE_TAB_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.PROJECT_DOC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STORAGE_MODE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_METHOD
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ROOT_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SCHEMA_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TAGS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.TAG_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.TAG_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class SubmissionConverter(
    private val sectionConverter: SectionConverter,
    private val attributeConverter: AttributeConverter,
    private val fileConverter: FileConverter
) : Converter<DocSubmission, Document> {
    override fun convert(submission: DocSubmission): Document {
        val submissionDoc = Document()
        submissionDoc[classField] = DOC_SUBMISSION_CLASS
        submissionDoc[SUB_ID] = submission.id
        submissionDoc[SUB_ACC_NO] = submission.accNo
        submissionDoc[SUB_VERSION] = submission.version
        submissionDoc[SUB_SCHEMA_VERSION] = submission.schemaVersion
        submissionDoc[SUB_OWNER] = submission.owner
        submissionDoc[SUB_SUBMITTER] = submission.submitter
        submissionDoc[SUB_TITLE] = submission.title
        submissionDoc[SUB_METHOD] = submission.method.value
        submissionDoc[SUB_REL_PATH] = submission.relPath
        submissionDoc[SUB_ROOT_PATH] = submission.rootPath
        submissionDoc[SUB_RELEASED] = submission.released
        submissionDoc[SUB_SECRET_KEY] = submission.secretKey
        submissionDoc[SUB_RELEASE_TIME] = submission.releaseTime
        submissionDoc[SUB_MODIFICATION_TIME] = submission.modificationTime
        submissionDoc[SUB_CREATION_TIME] = submission.creationTime
        submissionDoc[SUB_SECTION] = sectionConverter.convert(submission.section)
        submissionDoc[SUB_ATTRIBUTES] = submission.attributes.map { attributeConverter.convert(it) }
        submissionDoc[SUB_TAGS] = submission.tags.map { tagToDocument(it) }
        submissionDoc[SUB_COLLECTIONS] = submission.collections.map { collectionToDocument(it) }
        submissionDoc[PAGE_TAB_FILES] = submission.pageTabFiles.map { fileConverter.convert(it) }
        submissionDoc[STORAGE_MODE] = submission.storageMode.value
        return submissionDoc
    }

    private fun tagToDocument(docTag: DocTag): Document {
        val tagDoc = Document()
        tagDoc[classField] = DOC_TAG_CLASS
        tagDoc[TAG_DOC_NAME] = docTag.name
        tagDoc[TAG_DOC_VALUE] = docTag.value
        return tagDoc
    }

    private fun collectionToDocument(docCollection: DocCollection): Document {
        val projectDoc = Document()
        projectDoc[classField] = DOC_PROJECT_CLASS
        projectDoc[PROJECT_DOC_ACC_NO] = docCollection.accNo
        return projectDoc
    }
}
