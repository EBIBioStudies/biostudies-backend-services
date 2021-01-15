package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class SubmissionConverterTest(
    @MockK val attributeConverter: AttributeConverter,
    @MockK val sectionConverter: SectionConverter,
    @MockK val sectionDocument: Document,
    @MockK val attributeDocument: Document,
    @MockK val docSection: DocSection,
    @MockK val docAttribute: DocAttribute
) {
    private val testInstance = SubmissionConverter(sectionConverter, attributeConverter)

    @Test
    fun converter() {
        every { sectionConverter.convert(docSection) } returns sectionDocument
        every { attributeConverter.convert(docAttribute) } returns attributeDocument

        val docSubmission = createDocSubmission(docSection, docAttribute)

        val result = testInstance.convert(docSubmission)

        assertThat(result[DocSubmissionFields.SUB_ID]).isEqualTo(submissionId)
        assertThat(result[DocSubmissionFields.SUB_ACC_NO]).isEqualTo(submissionAccNo)
        assertThat(result[DocSubmissionFields.SUB_VERSION]).isEqualTo(submissionVersion)
        assertThat(result[DocSubmissionFields.SUB_OWNER]).isEqualTo(submissionOwner)
        assertThat(result[DocSubmissionFields.SUB_SUBMITTER]).isEqualTo(submissionSubmitter)
        assertThat(result[DocSubmissionFields.SUB_TITLE]).isEqualTo(submissionTitle)
        assertThat(result[DocSubmissionFields.SUB_METHOD]).isEqualTo(DocSubmissionMethod.PAGE_TAB.value)
        assertThat(result[DocSubmissionFields.SUB_REL_PATH]).isEqualTo(submissionRelPath)
        assertThat(result[DocSubmissionFields.SUB_ROOT_PATH]).isEqualTo(submissionRootPath)
        assertThat(result[DocSubmissionFields.SUB_RELEASED]).isEqualTo(submissionReleased)
        assertThat(result[DocSubmissionFields.SUB_SECRET_KEY]).isEqualTo(submissionSecretKey)
        assertThat(result[DocSubmissionFields.SUB_STATUS]).isEqualTo(DocProcessingStatus.PROCESSED.value)
        assertThat(result[DocSubmissionFields.SUB_RELEASE_TIME]).isEqualTo(submissionReleaseTime)
        assertThat(result[DocSubmissionFields.SUB_MODIFICATION_TIME]).isEqualTo(submissionModificationTime)
        assertThat(result[DocSubmissionFields.SUB_CREATION_TIME]).isEqualTo(submissionCreationTime)
        assertThat(result[DocSubmissionFields.SUB_SECTION]).isEqualTo(sectionDocument)
        assertThat(result[DocSubmissionFields.SUB_ATTRIBUTES]).isEqualTo(listOf(attributeDocument))

        val tags = result[DocSubmissionFields.SUB_TAGS] as List<Document>
        val tag = tags.first()
        assertThat(tag[DocSubmissionFields.TAG_DOC_NAME]).isEqualTo(docTagName)
        assertThat(tag[DocSubmissionFields.TAG_DOC_VALUE]).isEqualTo(docTagValue)

        val projects = result[DocSubmissionFields.SUB_PROJECTS] as List<Document>
        val project = projects.first()
        assertThat(project[DocSubmissionFields.PROJECT_DOC_ACC_NO]).isEqualTo(docProjectAccNo)

        val stats = result[DocSubmissionFields.SUB_STATS] as List<Document>
        val stat = stats.first()
        assertThat(stat[DocSubmissionFields.STAT_DOC_NAME]).isEqualTo(docStatName)
        assertThat(stat[DocSubmissionFields.STAT_DOC_VALUE]).isEqualTo(docStatValue)
    }

    private fun createDocSubmission(docSection: DocSection, docAttribute: DocAttribute): DocSubmission {
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
            projects = submissionProjects,
            stats = submissionStats)
    }

    private companion object {
        const val submissionId = "id"
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
        val submissionProjects = listOf(DocProject(docProjectAccNo))

        private const val docStatName = "component"
        private const val docStatValue: Long = 1
        val submissionStats = listOf(DocStat(docStatName, docStatValue))
    }
}
