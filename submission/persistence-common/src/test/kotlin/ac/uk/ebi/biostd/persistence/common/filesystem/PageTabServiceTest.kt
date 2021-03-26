package ac.uk.ebi.biostd.persistence.common.filesystem

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ebi.ac.uk.extended.mapping.to.toFilesTable
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.io.RWXR_X___
import ebi.ac.uk.io.RW_R_____
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class PageTabServiceTest(
    tempFolder: TemporaryFolder,
    @MockK private val serializationService: SerializationService
) {
    private val submissionFolder = tempFolder.root
    private val testInstance = PageTabService(serializationService)

    @Test
    fun `generate page tab`() {
        val fileList = ExtFileList("data/file-list", listOf())
        val submission = basicExtSubmission.copy(section = ExtSection(type = "Study", fileList = fileList))
        val request = PageTabRequest(submission, submissionFolder, RW_R_____, RWXR_X___)

        setUpSerializer(fileList.toFilesTable())
        setUpSerializer(submission.toSimpleSubmission())

        testInstance.generatePageTab(request)

        verifyFileLists()
        verifySubmissionFiles()
    }

    private fun verifySubmissionFiles() {
        assertThat(submissionFolder.resolve("S-TEST123.xml").exists())
        assertThat(submissionFolder.resolve("S-TEST123.json").exists())
        assertThat(submissionFolder.resolve("S-TEST123.pagetab.tsv").exists())
    }

    private fun verifyFileLists() {
        val submissionFiles = submissionFolder.resolve("Files")
        assertThat(submissionFiles.resolve("data/file-list.xml").exists())
        assertThat(submissionFiles.resolve("data/file-list.json").exists())
        assertThat(submissionFiles.resolve("data/file-list.pagetab.tsv").exists())
    }

    private fun setUpSerializer(element: Any) {
        every { serializationService.serializeElement(element, XML) } returns "xml"
        every { serializationService.serializeElement(element, TSV) } returns "tsv"
        every { serializationService.serializeElement(element, JSON_PRETTY) } returns "json"
    }
}
