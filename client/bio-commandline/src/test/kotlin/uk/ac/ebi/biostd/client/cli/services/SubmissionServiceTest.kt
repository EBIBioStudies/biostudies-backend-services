package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.model.Submission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest {
    private val testInstance = SubmissionService()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(SecurityWebClient)

    @Test
    fun submit() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.submitSingle(submissionRequest.submissionFile, submissionRequest.filesConfig).body
        } returns submission

        val submitted = testInstance.submit(submissionRequest)

        assertThat(submitted).isEqualTo(submission)
        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.submitSingle(
                submissionRequest.submissionFile,
                submissionRequest.filesConfig
            )
        }
    }

    @Test
    fun `submit async`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.asyncSubmitSingle(submissionRequest.submissionFile, submissionRequest.filesConfig)
        } answers { nothing }

        testInstance.submitAsync(submissionRequest)

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.asyncSubmitSingle(
                submissionRequest.submissionFile,
                submissionRequest.filesConfig
            )
        }
    }

    @Test
    fun `delete successful`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient
        every { bioWebClient.deleteSubmissions(deletionRequest.accNoList) } answers { nothing }

        testInstance.delete(deletionRequest)

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, null)
            bioWebClient.deleteSubmissions(deletionRequest.accNoList)
        }
    }

    @Test
    fun `perform request throw web client exception with null message`() {
        every { webClientException.message } returns null
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } throws webClientException

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(deletionRequest) }
            .withMessage("WebClientException: ")
    }

    @Test
    fun `perform request throw web client exception with not null message`() {
        every { webClientException.message } returns ERROR_MESSAGE
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } throws webClientException

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(deletionRequest) }
            .withMessage("WebClientException: $ERROR_MESSAGE")
    }

    @Test
    fun `perform request throw other exception with null message`() {
        every { webClientException.message } returns null
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } throws webClientException

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(deletionRequest) }
            .withMessage("WebClientException: ")
    }

    @Test
    fun `perform request throw other exception with not null message`() {
        every { webClientException.message } returns ERROR_MESSAGE
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } throws webClientException

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(deletionRequest) }
            .withMessage("WebClientException: $ERROR_MESSAGE")
    }

    @Test
    fun `validate file list`() {
        val (fileListPath, accNo, rootPath) = validateFileList
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every { bioWebClient.validateFileList(fileListPath, rootPath, accNo) } answers { nothing }

        testInstance.validateFileList(validateFileList)

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.validateFileList(fileListPath, rootPath, accNo)
        }
    }

    private companion object {
        private const val ACC_NO = "S-BSST0"
        private const val ERROR_MESSAGE = "error message"
        private const val ON_BEHALF = "onBehalf"
        private const val PASSWORD = "password"
        private const val SERVER = "server"
        private const val USER = "user"
        private const val FILE_LIST_PATH = "file-list.json"
        private const val ROOT_PATH = "root-path"

        private val webClientException: WebClientException = mockk()
        private val submission: Submission = mockk()
        private val bioWebClient: BioWebClient = mockk()
        private val securityConfig = SecurityConfig(SERVER, USER, PASSWORD, ON_BEHALF)
        private val filesConfig = SubmissionFilesConfig(listOf(mockk()), listOf(SUBMISSION))

        private val submissionRequest = SubmissionRequest(mockk(), securityConfig, filesConfig)
        private val deletionRequest = DeletionRequest(securityConfig, accNoList = listOf(ACC_NO))
        private val validateFileList = ValidateFileListRequest(FILE_LIST_PATH, ROOT_PATH, ACC_NO, securityConfig)
    }
}
