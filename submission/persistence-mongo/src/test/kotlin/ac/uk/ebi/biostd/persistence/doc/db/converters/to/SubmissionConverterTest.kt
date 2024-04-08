package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.PAGE_TAB_FILES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STORAGE_MODE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_METHOD
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ROOT_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SCHEMA_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ebi.ac.uk.extended.model.StorageMode
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class SubmissionConverterTest(
    @MockK val attributeConverter: AttributeConverter,
    @MockK val docAttribute: DocAttribute,
    @MockK val attributeDocument: Document,
    @MockK val sectionConverter: SectionConverter,
    @MockK val docSection: DocSection,
    @MockK val sectionDocument: Document,
    @MockK val fileConverter: FileConverter,
    @MockK val docFile: DocFile,
    @MockK val fileDocument: Document,
) {
    private val testInstance = SubmissionConverter(sectionConverter, attributeConverter, fileConverter)

    @Test
    fun converter() {
        every { sectionConverter.convert(docSection) } returns sectionDocument
        every { attributeConverter.convert(docAttribute) } returns attributeDocument
        every { fileConverter.convert(docFile) } returns fileDocument

        val docSubmission = createDocSubmission(docSection, docAttribute, docFile)

        val result = testInstance.convert(docSubmission)

        assertThat(result[SUB_ID]).isEqualTo(submissionId)
        assertThat(result[SUB_ACC_NO]).isEqualTo(ACC_NO)
        assertThat(result[SUB_VERSION]).isEqualTo(VERSION)
        assertThat(result[SUB_SCHEMA_VERSION]).isEqualTo(SCHEMA_VERSION)
        assertThat(result[SUB_OWNER]).isEqualTo(OWNER)
        assertThat(result[SUB_SUBMITTER]).isEqualTo(SUBMITTER)
        assertThat(result[SUB_TITLE]).isEqualTo(TITLE)
        assertThat(result[SUB_METHOD]).isEqualTo(DocSubmissionMethod.PAGE_TAB.value)
        assertThat(result[SUB_REL_PATH]).isEqualTo(REL_PATH)
        assertThat(result[SUB_ROOT_PATH]).isEqualTo(ROOT_PATH)
        assertThat(result[SUB_RELEASED]).isEqualTo(RELEASED)
        assertThat(result[SUB_SECRET_KEY]).isEqualTo(SECRET_KEY)
        assertThat(result[SUB_RELEASE_TIME]).isEqualTo(submissionReleaseTime)
        assertThat(result[SUB_MODIFICATION_TIME]).isEqualTo(submissionModificationTime)
        assertThat(result[SUB_CREATION_TIME]).isEqualTo(submissionCreationTime)
        assertThat(result[SUB_SECTION]).isEqualTo(sectionDocument)
        assertThat(result[SUB_ATTRIBUTES]).isEqualTo(listOf(attributeDocument))
        assertThat(result[PAGE_TAB_FILES]).isEqualTo(listOf(fileDocument))
        assertThat(result[STORAGE_MODE]).isEqualTo(StorageMode.NFS.value)

        val tags = result.getAs<List<Document>>(DocSubmissionFields.SUB_TAGS)
        val tag = tags.first()
        assertThat(tag[DocSubmissionFields.TAG_DOC_NAME]).isEqualTo(TAG_NAME)
        assertThat(tag[DocSubmissionFields.TAG_DOC_VALUE]).isEqualTo(TAG_VALUE)

        val collections = result.getAs<List<Document>>(DocSubmissionFields.SUB_COLLECTIONS)
        val collection = collections.first()
        assertThat(collection[DocSubmissionFields.COLLECTION_ACC_NO]).isEqualTo(COLLECTION_ACC_NO)
    }

    private fun createDocSubmission(
        docSection: DocSection,
        docAttribute: DocAttribute,
        docFile: DocFile,
    ): DocSubmission {
        return DocSubmission(
            id = submissionId,
            accNo = ACC_NO,
            version = VERSION,
            schemaVersion = SCHEMA_VERSION,
            owner = OWNER,
            submitter = SUBMITTER,
            title = TITLE,
            doi = DOI,
            method = DocSubmissionMethod.PAGE_TAB,
            relPath = REL_PATH,
            rootPath = ROOT_PATH,
            released = RELEASED,
            secretKey = SECRET_KEY,
            releaseTime = submissionReleaseTime,
            modificationTime = submissionModificationTime,
            creationTime = submissionCreationTime,
            section = docSection,
            attributes = listOf(docAttribute),
            tags = submissionTags,
            collections = submissionCollections,
            pageTabFiles = listOf(docFile),
            storageMode = StorageMode.NFS,
        )
    }

    private companion object {
        private val submissionId = ObjectId()
        private const val ACC_NO = "S-TEST1"
        private const val VERSION = 1
        private const val SCHEMA_VERSION = "1.0"
        private const val OWNER = "owner@mail.org"
        private const val SUBMITTER = "submitter@mail.org"
        private const val TITLE = "TestSubmission"
        private const val DOI = "10.983/S-TEST1"
        private const val REL_PATH = "/a/rel/path"
        private const val ROOT_PATH = "/a/root/path"
        private const val RELEASED = false
        private const val SECRET_KEY = "a-secret-key"
        private val submissionReleaseTime: Instant = Instant.ofEpochSecond(1)
        private val submissionModificationTime: Instant = Instant.ofEpochSecond(2)
        private val submissionCreationTime: Instant = Instant.ofEpochSecond(3)

        private const val TAG_NAME = "component"
        private const val TAG_VALUE = "web"
        private val submissionTags = listOf(DocTag(TAG_NAME, TAG_VALUE))

        private const val COLLECTION_ACC_NO = "BioImages"
        private val submissionCollections = listOf(DocCollection(COLLECTION_ACC_NO))
    }
}
