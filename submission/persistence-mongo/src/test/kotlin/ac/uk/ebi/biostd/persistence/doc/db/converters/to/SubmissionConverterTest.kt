package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.PAGE_TAB_FILES
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
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
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
    @MockK val fileDocument: Document
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
        assertThat(result[SUB_ACC_NO]).isEqualTo(submissionAccNo)
        assertThat(result[SUB_VERSION]).isEqualTo(submissionVersion)
        assertThat(result[SUB_OWNER]).isEqualTo(submissionOwner)
        assertThat(result[SUB_SUBMITTER]).isEqualTo(submissionSubmitter)
        assertThat(result[SUB_TITLE]).isEqualTo(submissionTitle)
        assertThat(result[SUB_METHOD]).isEqualTo(DocSubmissionMethod.PAGE_TAB.value)
        assertThat(result[SUB_REL_PATH]).isEqualTo(submissionRelPath)
        assertThat(result[SUB_ROOT_PATH]).isEqualTo(submissionRootPath)
        assertThat(result[SUB_RELEASED]).isEqualTo(submissionReleased)
        assertThat(result[SUB_SECRET_KEY]).isEqualTo(submissionSecretKey)
        assertThat(result[SUB_STATUS]).isEqualTo(DocProcessingStatus.PROCESSED.value)
        assertThat(result[SUB_RELEASE_TIME]).isEqualTo(submissionReleaseTime)
        assertThat(result[SUB_MODIFICATION_TIME]).isEqualTo(submissionModificationTime)
        assertThat(result[SUB_CREATION_TIME]).isEqualTo(submissionCreationTime)
        assertThat(result[SUB_SECTION]).isEqualTo(sectionDocument)
        assertThat(result[SUB_ATTRIBUTES]).isEqualTo(listOf(attributeDocument))
        assertThat(result[PAGE_TAB_FILES]).isEqualTo(listOf(fileDocument))

        val tags = result.getAs<List<Document>>(DocSubmissionFields.SUB_TAGS)
        val tag = tags.first()
        assertThat(tag[DocSubmissionFields.TAG_DOC_NAME]).isEqualTo(docTagName)
        assertThat(tag[DocSubmissionFields.TAG_DOC_VALUE]).isEqualTo(docTagValue)

        val projects = result.getAs<List<Document>>(DocSubmissionFields.SUB_PROJECTS)
        val project = projects.first()
        assertThat(project[DocSubmissionFields.PROJECT_DOC_ACC_NO]).isEqualTo(docProjectAccNo)

        val stats = result.getAs<List<Document>>(DocSubmissionFields.SUB_STATS)
        val stat = stats.first()
        assertThat(stat[DocSubmissionFields.STAT_DOC_NAME]).isEqualTo(docStatName)
        assertThat(stat[DocSubmissionFields.STAT_DOC_VALUE]).isEqualTo(docStatValue)
    }

    private fun createDocSubmission(
        docSection: DocSection,
        docAttribute: DocAttribute,
        docFile: DocFile
    ): DocSubmission {
        return DocSubmission(
            id = submissionId,
            accNo = submissionAccNo,
            version = submissionVersion,
            owner = submissionOwner,
            submitter = submissionSubmitter,
            title = submissionTitle,
            method = DocSubmissionMethod.PAGE_TAB,
            relPath = submissionRelPath,
            rootPath = submissionRootPath,
            released = submissionReleased,
            secretKey = submissionSecretKey,
            status = DocProcessingStatus.PROCESSED,
            releaseTime = submissionReleaseTime,
            modificationTime = submissionModificationTime,
            creationTime = submissionCreationTime,
            section = docSection,
            attributes = listOf(docAttribute),
            tags = submissionTags,
            collections = submissionProjects,
            stats = submissionStats,
            pageTabFiles = listOf(docFile)
        )
    }

    private companion object {
        val submissionId = ObjectId()
        const val submissionAccNo = "S-TEST1"
        const val submissionVersion = 1
        const val submissionOwner = "owner@mail.org"
        const val submissionSubmitter = "submitter@mail.org"
        const val submissionTitle = "TestSubmission"
        const val submissionRelPath = "/a/rel/path"
        const val submissionRootPath = "/a/root/path"
        const val submissionReleased = false
        const val submissionSecretKey = "a-secret-key"
        val submissionReleaseTime: Instant = Instant.ofEpochSecond(1)
        val submissionModificationTime: Instant = Instant.ofEpochSecond(2)
        val submissionCreationTime: Instant = Instant.ofEpochSecond(3)

        private const val docTagName = "component"
        private const val docTagValue = "web"
        val submissionTags = listOf(DocTag(docTagName, docTagValue))

        private const val docProjectAccNo = "BioImages"
        val submissionProjects = listOf(DocCollection(docProjectAccNo))

        private const val docStatName = "component"
        private const val docStatValue: Long = 1
        val submissionStats = listOf(DocStat(docStatName, docStatValue))
    }
}
