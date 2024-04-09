package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.docSubmissionClass
import ebi.ac.uk.extended.model.StorageMode
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class DocSubmissionConverterTest(
    @MockK val docAttributeConverter: DocAttributeConverter,
    @MockK val attributeDocument: Document,
    @MockK val docAttribute: DocAttribute,
    @MockK val docSectionConverter: DocSectionConverter,
    @MockK val sectionDocument: Document,
    @MockK val docSection: DocSection,
    @MockK val docFileConverter: DocFileConverter,
    @MockK val subTabFile: Document,
    @MockK val docFile: DocFile,
) {
    private val testInstance = DocSubmissionConverter(docFileConverter, docSectionConverter, docAttributeConverter)

    @Test
    fun convert() {
        every { docSectionConverter.convert(sectionDocument) } returns docSection
        every { docAttributeConverter.convert(attributeDocument) } returns docAttribute
        every { docFileConverter.convert(subTabFile) } returns docFile

        val result = testInstance.convert(createSubmissionDocument(sectionDocument, attributeDocument, subTabFile))

        assertThatAll(result)
    }

    private fun assertThatAll(result: DocSubmission) {
        assertThat(result).isInstanceOf(DocSubmission::class.java)
        assertThat(result.id).isEqualTo(subId)
        assertThat(result.accNo).isEqualTo(SUB_ACC_NO)
        assertThat(result.version).isEqualTo(VERSION)
        assertThat(result.schemaVersion).isEqualTo(SCHEMA_VERSION)
        assertThat(result.owner).isEqualTo(OWNER)
        assertThat(result.submitter).isEqualTo(SUBMITTER)
        assertThat(result.title).isEqualTo(TITLE)
        assertThat(result.method).isEqualTo(DocSubmissionMethod.fromString(METHOD))
        assertThat(result.relPath).isEqualTo(REL_PATH)
        assertThat(result.rootPath).isEqualTo(ROOT_PATH)
        assertThat(result.released).isEqualTo(RELEASED)
        assertThat(result.secretKey).isEqualTo(SECRET_KEY)
        assertThat(result.releaseTime).isEqualTo(subReleaseTime.toInstant())
        assertThat(result.modificationTime).isEqualTo(subModificationTime.toInstant())
        assertThat(result.creationTime).isEqualTo(subCreationTime.toInstant())
        assertThat(result.section).isEqualTo(docSection)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.tags[0].name).isEqualTo(TAG_DOC_NAME)
        assertThat(result.tags[0].value).isEqualTo(TAG_DOC_VALUE)
        assertThat(result.collections[0].accNo).isEqualTo(COLLECTION_DOC_ACC_NO)
        assertThat(result.pageTabFiles).isEqualTo(listOf(docFile))
        assertThat(result.storageMode).isEqualTo(StorageMode.NFS)
    }

    private fun createSubmissionDocument(
        sectionDocument: Document,
        attributeDocument: Document,
        fileDocument: Document,
    ): Document {
        val subDocument = Document()
        subDocument[DocSubmissionFields.CLASS_FIELD] = docSubmissionClass
        subDocument[DocSubmissionFields.SUB_ID] = subId
        subDocument[DocSubmissionFields.SUB_ACC_NO] = SUB_ACC_NO
        subDocument[DocSubmissionFields.SUB_VERSION] = VERSION
        subDocument[DocSubmissionFields.SUB_SCHEMA_VERSION] = SCHEMA_VERSION
        subDocument[DocSubmissionFields.SUB_OWNER] = OWNER
        subDocument[DocSubmissionFields.SUB_SUBMITTER] = SUBMITTER
        subDocument[DocSubmissionFields.SUB_TITLE] = TITLE
        subDocument[DocSubmissionFields.SUB_METHOD] = METHOD
        subDocument[DocSubmissionFields.SUB_REL_PATH] = REL_PATH
        subDocument[DocSubmissionFields.SUB_ROOT_PATH] = ROOT_PATH
        subDocument[DocSubmissionFields.SUB_RELEASED] = RELEASED
        subDocument[DocSubmissionFields.SUB_SECRET_KEY] = SECRET_KEY
        subDocument[DocSubmissionFields.SUB_STATUS] = STATUS
        subDocument[DocSubmissionFields.SUB_RELEASE_TIME] = subReleaseTime
        subDocument[DocSubmissionFields.SUB_MODIFICATION_TIME] = subModificationTime
        subDocument[DocSubmissionFields.SUB_CREATION_TIME] = subCreationTime
        subDocument[DocSubmissionFields.SUB_SECTION] = sectionDocument
        subDocument[DocSubmissionFields.SUB_ATTRIBUTES] = listOf(attributeDocument)
        subDocument[DocSubmissionFields.SUB_TAGS] = listOf(createTagDocument())
        subDocument[DocSubmissionFields.SUB_COLLECTIONS] = listOf(createCollectionDocument())
        subDocument[DocSubmissionFields.PAGE_TAB_FILES] = listOf(fileDocument)
        subDocument[DocSubmissionFields.STORAGE_MODE] = STORAGE_MODE
        return subDocument
    }

    private fun createTagDocument(): Document {
        val tagDoc = Document()
        tagDoc[DocSubmissionFields.TAG_DOC_NAME] = TAG_DOC_NAME
        tagDoc[DocSubmissionFields.TAG_DOC_VALUE] = TAG_DOC_VALUE
        return tagDoc
    }

    private fun createCollectionDocument(): Document {
        val collectionDoc = Document()
        collectionDoc[DocSubmissionFields.COLLECTION_ACC_NO] = COLLECTION_DOC_ACC_NO
        return collectionDoc
    }

    companion object {
        private val subId = ObjectId(1, 1)
        private val subReleaseTime: Date = Date(110)
        private val subModificationTime: Date = Date(220)
        private val subCreationTime: Date = Date(330)

        private const val SUB_ACC_NO = "accNo"
        private const val COLLECTION_DOC_ACC_NO = "accNo"
        private const val VERSION = 1
        private const val SCHEMA_VERSION = "1.0"
        private const val OWNER = "owner"
        private const val SUBMITTER = "submitter"
        private const val TITLE = "title"
        private const val METHOD = "FILE"
        private const val STATUS = "PROCESSED"
        private const val REL_PATH = "relPath"
        private const val ROOT_PATH = "rootPath"
        private const val RELEASED = false
        private const val SECRET_KEY = "secretKey"
        private const val TAG_DOC_NAME = "name"
        private const val TAG_DOC_VALUE = "value"
        private const val STORAGE_MODE = "NFS"
    }
}
