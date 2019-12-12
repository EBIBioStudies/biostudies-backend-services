package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import com.github.ajalt.clikt.core.IncorrectOptionValueCount
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.model.Submission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import java.io.File

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class BioStudiesCommandLineTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockWebClient: BioWebClient
) {
    @SpyK
    private var testInstance = BioStudiesCommandLine()

    private lateinit var rootFolder: String
    private lateinit var submissionFile: File

    @BeforeEach
    fun setUp() {
        rootFolder = temporaryFolder.root.absolutePath
        submissionFile = temporaryFolder.createFile("Submission.tsv")

        temporaryFolder.createDirectory("attachments")
        temporaryFolder.createDirectory("attachments/inner")

        val mockResponse = ResponseEntity.ok(Submission("S-TEST123"))
        val libFile = temporaryFolder.createFile("FileList.tsv")
        val refFile = temporaryFolder.createFile("attachments/inner/RefFile.txt")

        every { mockWebClient.submitSingle(submissionFile, listOf()) } returns mockResponse
        every { mockWebClient.submitSingle(submissionFile, listOf(libFile, refFile)) } returns mockResponse
        every { testInstance.getClient("http://localhost:8080", "user", "123456", null) } returns mockWebClient
        every { testInstance.getClient("http://localhost:8080", "user", "123456", "other") } returns mockWebClient
    }

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    fun `submit single with no attachments`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv")

        testInstance.main(args)
        verify(exactly = 1) { testInstance.getClient("http://localhost:8080", "user", "123456", null) }
    }

    @Test
    fun `submit single on behalf`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-b", "other",
            "-i", "$rootFolder/Submission.tsv")

        testInstance.main(args)
        verify(exactly = 1) { testInstance.getClient("http://localhost:8080", "user", "123456", "other") }
    }

    @Test
    fun `submit single with attachments`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv",
            "-a", "$rootFolder/FileList.tsv,$rootFolder/attachments")

        testInstance.main(args)
    }

    @Test
    fun `submit single with invalid files`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv")

        every {
            mockWebClient.submitSingle(submissionFile, listOf())
        } throws WebClientException(BAD_REQUEST, "Invalid Files")

        val exception = assertThrows<PrintMessage> { testInstance.parse(args) }
        assertThat(exception.message).isEqualTo("Invalid Files")
    }

    @Test
    fun `submit single with generic exception`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv")

        every { mockWebClient.submitSingle(submissionFile, listOf()) } throws Exception("Generic Exception")

        val exception = assertThrows<Exception> { testInstance.parse(args) }
        assertThat(exception.message).isEqualTo("Generic Exception")
    }

    @Test
    fun `missing arguments`() {
        val exceptionMessage = assertThrows<MissingParameter> { testInstance.parse(arrayOf("-u", "manager")) }.message
        assertThat(exceptionMessage).isEqualTo("Missing option \"--server\".")
    }

    @Test
    fun `missing value for argument`() {
        val exceptionMessage = assertThrows<IncorrectOptionValueCount> { testInstance.parse(arrayOf("-u")) }.message
        assertThat(exceptionMessage).isEqualTo("-u option requires an argument")
    }
}
