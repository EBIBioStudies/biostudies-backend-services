package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FileMode.MOVE
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

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest {
    private val testInstance = SubmissionService()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(SecurityWebClient)

    @Test
    fun `submit copying files`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.submitSingle(submissionRequest.file, submissionRequest.attached, fileMode = COPY).body
        } returns submission

        val submitted = testInstance.submit(submissionRequest)

        assertThat(submitted).isEqualTo(submission)
        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.submitSingle(submissionRequest.file, submissionRequest.attached, fileMode = COPY)
        }
    }

    @Test
    fun `submit moving files`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.submitSingle(submissionRequest.file, submissionRequest.attached, fileMode = MOVE).body
        } returns submission

        val submitted = testInstance.submit(submissionRequest.copy(fileMode = MOVE))

        assertThat(submitted).isEqualTo(submission)
        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.submitSingle(submissionRequest.file, submissionRequest.attached, fileMode = MOVE)
        }
    }

    @Test
    fun `submit async copying files`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.asyncSubmitSingle(submissionRequest.file, submissionRequest.attached, fileMode = COPY)
        } answers { nothing }

        testInstance.submitAsync(submissionRequest)

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.asyncSubmitSingle(submissionRequest.file, submissionRequest.attached, fileMode = COPY)
        }
    }

    @Test
    fun `submit async moving files`() {
        every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
        every {
            bioWebClient.asyncSubmitSingle(submissionRequest.file, submissionRequest.attached, fileMode = MOVE)
        } answers { nothing }

        testInstance.submitAsync(submissionRequest.copy(fileMode = MOVE))

        verify(exactly = 1) {
            create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
            bioWebClient.asyncSubmitSingle(submissionRequest.file, submissionRequest.attached, fileMode = MOVE)
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

    private companion object {
        private const val ACC_NO = "S-BSST0"
        private const val ERROR_MESSAGE = "error message"
        private const val ON_BEHALF = "onBehalf"
        private const val PASSWORD = "password"
        private const val SERVER = "server"
        private const val USER = "user"

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
            fileMode = COPY
        )

        val deletionRequest = DeletionRequest(
            server = SERVER,
            user = USER,
            password = PASSWORD,
            onBehalf = ON_BEHALF,
            accNoList = listOf(ACC_NO)
        )
    }
}
