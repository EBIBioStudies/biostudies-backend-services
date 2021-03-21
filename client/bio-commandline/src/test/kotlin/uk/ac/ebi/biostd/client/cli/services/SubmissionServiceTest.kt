package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.model.Submission
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest {

    private val testInstance = SubmissionService()

    @Test
    fun `submit successful`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } returns bioWebClient
        every { bioWebClient.submitSingle(requestSubmit.file, requestSubmit.attached).body } returns submission

        assertThat(testInstance.submit(requestSubmit)).isEqualTo(submission)
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `submit throw web client exception with null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } throws webClientException
        every { webClientException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.submit(requestSubmit) }
            .withMessage("WebClientException: ")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `submit throw web client exception with not null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } throws webClientException
        every { webClientException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.submit(requestSubmit) }
            .withMessage("WebClientException: $message")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `submit throw other exception with null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } throws webClientException
        every { webClientException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.submit(requestSubmit) }
            .withMessage("WebClientException: ")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `submit throw other exception with not null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } throws webClientException
        every { webClientException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.submit(requestSubmit) }
            .withMessage("WebClientException: $message")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete successful`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } returns bioWebClient
        every { bioWebClient.deleteSubmission(requestDelete.accNo) } returns Unit

        testInstance.delete(requestDelete)

        verify(exactly = 1) { bioWebClient.deleteSubmission(requestDelete.accNo) }
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete throw web client exception with null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: ")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete throw web client exception with not null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: $message")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete throw other exception with  null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: ")
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete throw other exception with not null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: $message")
        unmockkObject(SecurityWebClient)
    }

    private companion object {
        val webClientException: WebClientException = mockk()
        val submission: Submission = mockk()
        val bioWebClient: BioWebClient = mockk()

        val requestSubmit = SubmissionRequest(
            server = "server",
            user = "user",
            password = "password",
            onBehalf = "onBehalf",
            file = mockk(),
            attached = listOf(mockk())
        )

        val requestDelete = SubmissionDeleteRequest(
            server = "server",
            user = "user",
            password = "password",
            onBehalf = "onBehalf",
            accNo = "accNo"
        )

        const val message = "error message"
    }
}
