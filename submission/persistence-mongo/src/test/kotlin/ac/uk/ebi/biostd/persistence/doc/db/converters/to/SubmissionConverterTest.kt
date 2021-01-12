package ac.uk.ebi.biostd.persistence.doc.db.converters.to

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

        assertThat(result[SubmissionConverter.subId]).isEqualTo(submissionId)
        assertThat(result[SubmissionConverter.subAccNo]).isEqualTo(submissionAccNo)
        assertThat(result[SubmissionConverter.subVersion]).isEqualTo(submissionVersion)
        assertThat(result[SubmissionConverter.subOwner]).isEqualTo(submissionOwner)
        assertThat(result[SubmissionConverter.subSubmitter]).isEqualTo(submissionSubmitter)
        assertThat(result[SubmissionConverter.subTitle]).isEqualTo(submissionTitle)
        assertThat(result[SubmissionConverter.subMethod]).isEqualTo(submissionMethod.value)
        assertThat(result[SubmissionConverter.subRelPath]).isEqualTo(submissionRelPath)
        assertThat(result[SubmissionConverter.subRootPath]).isEqualTo(submissionRootPath)
        assertThat(result[SubmissionConverter.subReleased]).isEqualTo(submissionReleased)
        assertThat(result[SubmissionConverter.subSecretKey]).isEqualTo(submissionSecretKey)
        assertThat(result[SubmissionConverter.subStatus]).isEqualTo(docSubmissionStatus.value)
        assertThat(result[SubmissionConverter.subReleaseTime]).isEqualTo(submissionReleaseTime)
        assertThat(result[SubmissionConverter.subModificationTime]).isEqualTo(submissionModificationTime)
        assertThat(result[SubmissionConverter.subCreationTime]).isEqualTo(submissionCreationTime)
        assertThat(result[SubmissionConverter.subSection]).isEqualTo(sectionDocument)
        assertThat(result[SubmissionConverter.subAttributes]).isEqualTo(listOf(attributeDocument))

        val tags = result[SubmissionConverter.subTags] as List<Document>
        val tag = tags.first()
        assertThat(tag[SubmissionConverter.tagDocName]).isEqualTo(docTagName)
        assertThat(tag[SubmissionConverter.tagDocValue]).isEqualTo(docTagValue)

        val projects = result[SubmissionConverter.subProjects] as List<Document>
        val project = projects.first()
        assertThat(project[SubmissionConverter.projectDocAccNo]).isEqualTo(docProjectAccNo)

        val stats = result[SubmissionConverter.subStats] as List<Document>
        val stat = stats.first()
        assertThat(stat[SubmissionConverter.statDocName]).isEqualTo(docStatName)
        assertThat(stat[SubmissionConverter.statDocValue]).isEqualTo(docStatValue)
    }

    private fun createDocSubmission(docSection: DocSection, docAttribute: DocAttribute): DocSubmission {
        return DocSubmission(
            id = submissionId,
            accNo = submissionAccNo,
            version = submissionVersion,
            owner = submissionOwner,
            submitter = submissionSubmitter,
            title = submissionTitle,
            method = submissionMethod,
            relPath = submissionRelPath,
            rootPath = submissionRootPath,
            released = submissionReleased,
            secretKey = submissionSecretKey,
            status = docSubmissionStatus,
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
        val submissionMethod = DocSubmissionMethod.PAGE_TAB
        const val submissionRelPath = "/a/rel/path"
        const val submissionRootPath = "/a/root/path"
        const val submissionReleased = false
        const val submissionSecretKey = "a-secret-key"
        val submissionReleaseTime: Instant = Instant.ofEpochSecond(1)
        val submissionModificationTime: Instant = Instant.ofEpochSecond(2)
        val submissionCreationTime: Instant = Instant.ofEpochSecond(3)
        val docSubmissionStatus = DocProcessingStatus.PROCESSED

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
