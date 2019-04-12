package ac.uk.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ebi.ac.uk.model.Submission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.commons.cli.HelpFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import java.io.IOException
import java.lang.NullPointerException

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class BioStudiesCommandLineTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockHelpFormatter: HelpFormatter,
    @MockK private val mockWebClient: BioWebClient
) {
    @SpyK
    private var testInstance = BioStudiesCommandLine(mockHelpFormatter)
    private val options = testInstance.options
    private lateinit var rootFolder: String

    @BeforeEach
    fun setUp() {
        rootFolder = temporaryFolder.root.absolutePath
        temporaryFolder.createFile("Submission.tsv")

        val refFile = temporaryFolder.createFile("RefFile.txt")
        val libFile = temporaryFolder.createFile("LibraryFile.tsv")
        val mockResponse = ResponseEntity.ok(Submission("S-TEST123"))

        every { mockHelpFormatter.printHelp(CLI_ID, options) }.answers { nothing }
        every { mockWebClient.submitSingle("", SubmissionFormat.TSV, listOf()) } returns mockResponse
        every { testInstance.getClient("http://localhost:8080", "user", "123456") } returns mockWebClient
        every { mockWebClient.submitSingle("", SubmissionFormat.TSV, listOf(libFile, refFile)) } returns mockResponse
    }

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    fun `submit single with no attachments`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
            "-i", "$rootFolder/Submission.tsv")
        val response = testInstance.submit(args)

        assertThat(response).isEqualTo("SUCCESS: Submission with AccNo S-TEST123 was submitted")
        verify(exactly = 0) { testInstance.printError("") }
        verify(exactly = 0) { mockHelpFormatter.printHelp(CLI_ID, options) }
    }

    @Test
    fun `submit single with attachments`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
            "-i", "$rootFolder/Submission.tsv",
            "-a", "$rootFolder/LibraryFile.tsv,$rootFolder/RefFile.txt")
        val response = testInstance.submit(args)

        assertThat(response).isEqualTo("SUCCESS: Submission with AccNo S-TEST123 was submitted")
        verify(exactly = 0) { testInstance.printError("") }
        verify(exactly = 0) { mockHelpFormatter.printHelp(CLI_ID, options) }
    }

    @Test
    fun `submit single with null response`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
            "-i", "$rootFolder/Submission.tsv")

        every { mockWebClient.submitSingle("", SubmissionFormat.TSV, listOf()) } throws NullPointerException()

        testInstance.submit(args)

        verify(exactly = 1) { testInstance.printError(NULL_SUBMISSION_ERROR_MSG) }
        verify(exactly = 0) { mockHelpFormatter.printHelp(CLI_ID, options) }
    }

    @Test
    fun `submit single with invalid files`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
            "-i", "$rootFolder/Submission.tsv")

        every {
            mockWebClient.submitSingle("", SubmissionFormat.TSV, listOf())
        } throws ResourceAccessException("Invalid files", IOException("Invalid Files"))

        testInstance.submit(args)

        verify(exactly = 1) { testInstance.printError("Invalid Files") }
        verify(exactly = 0) { mockHelpFormatter.printHelp(CLI_ID, options) }
    }

    @Test
    fun `submit single with server error`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
            "-i", "$rootFolder/Submission.tsv")
        val exception =
            RestClientResponseException("Error", 500, "Error", null, "{\"msg\":\"error\"}".toByteArray(), null)

        every { mockWebClient.submitSingle("", SubmissionFormat.TSV, listOf()) } throws exception
        testInstance.submit(args)

        verify(exactly = 1) { testInstance.printJsonError(exception) }
        verify(exactly = 0) { mockHelpFormatter.printHelp(CLI_ID, options) }
    }

    @Test
    fun `missing arguments`() {
        testInstance.submit(arrayOf("-f", "JSON"))

        verify(exactly = 1) { mockHelpFormatter.printHelp(CLI_ID, options) }
        verify(exactly = 1) { testInstance.printError("Missing required options: s, u, p, i") }
    }

    @Test
    fun `missing value for argument`() {
        testInstance.submit(arrayOf("-f"))

        verify(exactly = 1) { mockHelpFormatter.printHelp(CLI_ID, options) }
        verify(exactly = 1) { testInstance.printError("Missing argument for option: f") }
    }
}
