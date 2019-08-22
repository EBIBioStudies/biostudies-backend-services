package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
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
import org.springframework.http.ResponseEntity
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import java.io.File
import java.io.IOException

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class BioStudiesCommandLineTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockWebClient: BioWebClient
) {
    @SpyK
    private var testInstance = BioStudiesCommandLine()

    private lateinit var excelFile: File
    private lateinit var rootFolder: String

    @BeforeEach
    fun setUp() {
        rootFolder = temporaryFolder.root.absolutePath
        excelFile = temporaryFolder.createFile("ExcelSubmission.xlsx")

        temporaryFolder.createFile("Submission.tsv")
        temporaryFolder.createDirectory("attachments")
        temporaryFolder.createDirectory("attachments/inner")

        val mockResponse = ResponseEntity.ok(Submission("S-TEST123"))
        val libFile = temporaryFolder.createFile("FileList.tsv")
        val refFile = temporaryFolder.createFile("attachments/inner/RefFile.txt")

        every { mockWebClient.submitXlsx(excelFile, listOf()) } returns mockResponse
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

        testInstance.main(args)
    }

    @Test
    fun `submit single with attachments`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
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
            "-f", "TSV",
            "-i", "$rootFolder/Submission.tsv")

        every {
            mockWebClient.submitSingle("", SubmissionFormat.TSV, listOf())
        } throws ResourceAccessException("Invalid files", IOException("Invalid Files"))

        val exceptionMessage = assertThrows<PrintMessage> { testInstance.parse(args) }.message
        assertThat(exceptionMessage).isEqualTo("Invalid Files")
    }

    @Test
    fun `submit with excel file`() {
        val args = arrayOf(
            "-s", "http://localhost:8080",
            "-u", "user",
            "-p", "123456",
            "-f", "TSV",
            "-i", "$rootFolder/ExcelSubmission.xlsx")

        testInstance.main(args)

        verify(exactly = 1) { mockWebClient.submitXlsx(excelFile, listOf()) }
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

        val exceptionMessage = assertThrows<PrintMessage> { testInstance.parse(args) }.message
        assertThat(exceptionMessage).isEqualTo("{\"msg\": \"error\"}")
    }

    @Test
    fun `missing arguments`() {
        val exceptionMessage = assertThrows<MissingParameter> { testInstance.parse(arrayOf("-f", "JSON")) }.message
        assertThat(exceptionMessage).isEqualTo("Missing option \"--server\".")
    }

    @Test
    fun `missing value for argument`() {
        val exceptionMessage = assertThrows<IncorrectOptionValueCount> { testInstance.parse(arrayOf("-f")) }.message
        assertThat(exceptionMessage).isEqualTo("-f option requires an argument")
    }
}
