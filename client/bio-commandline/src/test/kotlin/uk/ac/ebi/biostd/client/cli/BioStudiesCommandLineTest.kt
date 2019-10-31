package uk.ac.ebi.biostd.client.cli

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.io.File
import java.io.FileNotFoundException
import java.net.ConnectException
import java.net.HttpRetryException

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
        every { testInstance.getClient("http://localhost:8080", "user", "123456") } returns mockWebClient
        every { mockWebClient.submitSingle(submissionFile, listOf(libFile, refFile)) } returns mockResponse
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
        } throws ResourceAccessException("Invalid files", FileNotFoundException("Invalid Files"))

        val exceptionMessage = assertThrows<PrintMessage> { testInstance.parse(args) }.message
        assertThat(exceptionMessage).isEqualTo(FILES_NOT_FOUND_ERROR_MSG)
    }

    @Test
    fun `submit single with server error`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv")
        val exception =
            HttpClientErrorException(INTERNAL_SERVER_ERROR, "Error", null, "{\"msg\":\"error\"}".toByteArray(), null)

        every { mockWebClient.submitSingle(submissionFile, listOf()) } throws exception

        val exceptionMessage = assertThrows<PrintMessage> { testInstance.parse(args) }.message
        assertThat(exceptionMessage).isEqualTo("{\"msg\": \"error\"}")
    }

    @Test
    fun `submit single with invalid server`() {
        val args = arrayOf(
            "-s", "http://localhost:808",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv")

        every {
            mockWebClient.submitSingle(submissionFile, listOf())
        } throws ResourceAccessException("Invalid Server", ConnectException("Invalid Server"))

        val exception = assertThrows<PrintMessage> { testInstance.parse(args) }
        assertThat(exception.message).isEqualTo(INVALID_SERVER_ERROR_MSG)
    }

    @Test
    fun `submit single with authentication error`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-i", "$rootFolder/Submission.tsv")

        every {
            mockWebClient.submitSingle(submissionFile, listOf())
        } throws ResourceAccessException("Authentication Error", HttpRetryException("Authentication Error", 500))

        val exception = assertThrows<PrintMessage> { testInstance.parse(args) }
        assertThat(exception.message).isEqualTo(AUTHENTICATION_ERROR_MSG)
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
