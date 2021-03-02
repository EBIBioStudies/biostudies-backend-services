package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.SecurityWebClientException
import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.model.Submission
import io.mockk.mockkObject
import io.mockk.every
import io.mockk.unmockkObject
import io.mockk.verify
import io.mockk.mockk
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.formatErrorMessage

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest {

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
            .isThrownBy { testInstance.submit(requestSubmit) }.withMessage(webClientException.javaClass.canonicalName)
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
            .isThrownBy { testInstance.submit(requestSubmit) }.withMessage(formatErrorMessage(message))
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `submit throw other exception with null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } throws securityException
        every { securityException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.submit(requestSubmit) }.withMessage(securityException.javaClass.canonicalName)
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `submit throw other exception with not null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } throws securityException
        every { securityException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.submit(requestSubmit) }.withMessage(message)
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
            .isThrownBy { testInstance.delete(requestDelete) }.withMessage(webClientException.javaClass.canonicalName)
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
            .isThrownBy { testInstance.delete(requestDelete) }.withMessage(formatErrorMessage(message))
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete throw other exception with  null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws securityException
        every { securityException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }.withMessage(securityException.javaClass.canonicalName)
        unmockkObject(SecurityWebClient)
    }

    @Test
    fun `delete throw other exception with not null message`() {
        mockkObject(SecurityWebClient)
        every {
            create(requestDelete.server)
                .getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws securityException
        every { securityException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }.withMessage(message)
        unmockkObject(SecurityWebClient)
    }

    private companion object {
        val testInstance = SubmissionService()
        val webClientException: WebClientException = mockk()
        val securityException: SecurityWebClientException = mockk()
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

        const val message = "<note>\n" +
            "  <date>2015-09-01</date>\n" +
            "  <hour>08:30</hour>\n" +
            "  <to>Tove</to>\n" +
            "  <from>Jani</from>\n" +
            "  <body>Don't forget me this weekend!</body>\n" +
            "</note>"
    }
}
