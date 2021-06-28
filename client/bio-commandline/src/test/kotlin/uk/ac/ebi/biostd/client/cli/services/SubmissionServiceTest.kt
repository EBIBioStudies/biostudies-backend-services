package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.DeletionRequest
import uk.ac.ebi.biostd.client.cli.dto.MigrationRequest
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest {
    private val testInstance = SubmissionService()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(SecurityWebClient)

    @Test
    fun `submit successful`() {
        every {
            create(requestSubmit.server).getAuthenticatedClient(requestSubmit.user, requestSubmit.password)
        } returns bioWebClient
        every { bioWebClient.submitSingle(requestSubmit.file, requestSubmit.attached).body } returns submission

        assertThat(testInstance.submit(requestSubmit)).isEqualTo(submission)
    }

    @Test
    fun `delete successful`() {
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } returns bioWebClient
        every { bioWebClient.deleteSubmissions(requestDelete.accNoList) } returns Unit

        testInstance.delete(requestDelete)

        verify(exactly = 1) { bioWebClient.deleteSubmissions(requestDelete.accNoList) }
    }

    @Nested
    @ExtendWith(MockKExtension::class)
    inner class Migrate {

        @MockK
        private lateinit var sourceClient: BioWebClient

        @MockK
        private lateinit var targetClient: BioWebClient

        @MockK
        private lateinit var extSubmission: ExtSubmission

        private val rqt = MigrationRequest(
            accNo = "S-BSST1",
            source = "http://biostudy-prod",
            sourceUser = "admin_user@ebi.ac.uk",
            sourcePassword = "123456",
            target = "http://biostudy-prod",
            targetUser = "admin_user@ebi.ac.uk",
            targetPassword = "78910",
            targetOwner = null
        )

        @BeforeEach
        fun beforeEach() {
            mockkObject(SecurityWebClient)
            every { create(rqt.source).getAuthenticatedClient(rqt.sourceUser, rqt.sourcePassword) } returns sourceClient
            every { create(rqt.target).getAuthenticatedClient(rqt.targetUser, rqt.targetPassword) } returns targetClient
        }

        @Test
        fun `migrate when no target Owner`() {
            every { sourceClient.getExtByAccNo("S-BSST1") } returns extSubmission
            every { targetClient.submitExt(extSubmission) } returns extSubmission

            testInstance.migrate(rqt)

            verify(exactly = 1) { sourceClient.getExtByAccNo("S-BSST1") }
            verify(exactly = 1) { targetClient.submitExt(extSubmission) }
        }

        @Test
        fun `migrate when target Owner`() {
            val newOwner = "owner"
            every { sourceClient.getExtByAccNo("S-BSST1") } returns extSubmission
            every { targetClient.submitExt(extSubmission.copy(owner = newOwner)) } returns extSubmission

            testInstance.migrate(rqt.copy(targetOwner = newOwner))

            verify(exactly = 1) { sourceClient.getExtByAccNo("S-BSST1") }
            verify(exactly = 1) { targetClient.submitExt(extSubmission.copy(owner = newOwner)) }
        }
    }

    @Test
    fun `perform request throw web client exception with null message`() {
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: ")
    }

    @Test
    fun `perform request throw web client exception with not null message`() {
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: $message")
    }

    @Test
    fun `perform request throw other exception with  null message`() {
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns null

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: ")
    }

    @Test
    fun `perform request throw other exception with not null message`() {
        every {
            create(requestDelete.server).getAuthenticatedClient(requestDelete.user, requestDelete.password)
        } throws webClientException
        every { webClientException.message } returns message

        assertThatExceptionOfType(PrintMessage::class.java)
            .isThrownBy { testInstance.delete(requestDelete) }
            .withMessage("WebClientException: $message")
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

        val requestDelete = DeletionRequest(
            server = "server",
            user = "user",
            password = "password",
            onBehalf = "onBehalf",
            accNoList = listOf("accNo")
        )

        const val message = "error message"
    }
}
