package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.extended.model.FileMode.COPY
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
            bioWebClient.submitSingle(
                submissionRequest.file,
                submissionRequest.attached,
                fileMode = COPY,
                preferredSource = SUBMISSION
            ).body
        } returns submission

        val submitted = testInstance.submit(submissionRequest)

        assertThat(submitted).isEqualTo(submission)
        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.submitSingle(
                submissionRequest.file,
                submissionRequest.attached,
                fileMode = COPY,
                preferredSource = SUBMISSION
            )
        }
    }

    @Test
    fun `submit async`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.asyncSubmitSingle(
                submissionRequest.file,
                submissionRequest.attached,
                fileMode = COPY,
                preferredSource = SUBMISSION
            )
        } answers { nothing }

        testInstance.submitAsync(submissionRequest)

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.asyncSubmitSingle(
                submissionRequest.file,
                submissionRequest.attached,
                fileMode = COPY,
                preferredSource = SUBMISSION
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
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient
        every { bioWebClient.validateFileList(validateFileList.fileListPath) } answers { nothing }

        testInstance.validateFileList(validateFileList)

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, null)
            bioWebClient.validateFileList(validateFileList.fileListPath)
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

        val webClientException: WebClientException = mockk()
        val submission: Submission = mockk()
        val bioWebClient: BioWebClient = mockk()

        val submissionRequest = SubmissionRequest(
            server = SERVER,
            user = USER,
            password = PASSWORD,
            onBehalf = ON_BEHALF,
            file = mockk(),
            attached = listOf(mockk()),
            fileMode = COPY,
            preferredSource = SUBMISSION
        )

        val deletionRequest = DeletionRequest(
            server = SERVER,
            user = USER,
            password = PASSWORD,
            onBehalf = ON_BEHALF,
            accNoList = listOf(ACC_NO)
        )

        var validateFileList = ValidateFileListRequest(
            server = SERVER,
            user = USER,
            password = PASSWORD,
            onBehalf = null,
            fileListPath = FILE_LIST_PATH
        )
    }
}
