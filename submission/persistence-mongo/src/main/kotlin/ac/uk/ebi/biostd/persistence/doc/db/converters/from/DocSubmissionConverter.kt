package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.PROJECT_DOC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STAT_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STAT_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_METHOD
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_PROJECTS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ROOT_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TAGS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.TAG_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.TAG_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class DocSubmissionConverter(
    private val docSectionConverter: DocSectionConverter,
    private val docAttributeConverter: DocAttributeConverter
) : Converter<Document, DocSubmission> {
    override fun convert(source: Document): DocSubmission = DocSubmission(
        id = source.getObjectId(SUB_ID),
        accNo = source.getString(SUB_ACC_NO),
        version = source.getInteger(SUB_VERSION),
        owner = source.getString(SUB_OWNER),
        submitter = source.getString(SUB_SUBMITTER),
        title = source.getString(SUB_TITLE),
        method = DocSubmissionMethod.fromString(source.getString(SUB_METHOD)),
        relPath = source.getString(SUB_REL_PATH),
        rootPath = source.getString(SUB_ROOT_PATH),
        released = source.getBoolean(SUB_RELEASED),
        secretKey = source.getString(SUB_SECRET_KEY),
        status = DocProcessingStatus.fromString(source.getString(SUB_STATUS)),
        releaseTime = source.getDate(SUB_RELEASE_TIME)?.toInstant(),
        modificationTime = source.getDate(SUB_MODIFICATION_TIME).toInstant(),
        creationTime = source.getDate(SUB_CREATION_TIME).toInstant(),
        section = docSectionConverter.convert(source.getDoc(SUB_SECTION)),
        attributes = source.getDocList(SUB_ATTRIBUTES).map { docAttributeConverter.convert(it) },
        tags = source.getDocList(SUB_TAGS).map { toDocTag(it) },
        collections = source.getDocList(SUB_PROJECTS).map { toDocCollection(it) },
        stats = source.getDocList(SUB_STATS).map { toDocStat(it) }
    )

    private fun toDocTag(doc: Document): DocTag =
        DocTag(name = doc.getString(TAG_DOC_NAME), value = doc.getString(TAG_DOC_VALUE))

    private fun toDocStat(doc: Document): DocStat =
        DocStat(name = doc.getString(STAT_DOC_NAME), value = doc.getLong(STAT_DOC_VALUE))

    private fun toDocCollection(doc: Document): DocCollection = DocCollection(accNo = doc.getString(PROJECT_DOC_ACC_NO))
}
