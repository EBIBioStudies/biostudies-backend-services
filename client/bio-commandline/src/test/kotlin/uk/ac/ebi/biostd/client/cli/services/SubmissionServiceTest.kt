package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.SubmissionTransferOptions
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.client.cli.dto.SecurityConfig
import uk.ac.ebi.biostd.client.cli.dto.SubmissionRequest

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest(
    @param:MockK private val extUser: ExtUser,
    @param:MockK private val bioWebClient: BioWebClient,
    @param:MockK private val webClientException: WebClientException,
) {
    private val testInstance = SubmissionService()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(SecurityWebClient)

    @Test
    fun submit() =
        runTest {
            val accepted = SubmissionId("S-BSST1", 2)

            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
            coEvery {
                bioWebClient.submitMultipartAsync(subRequest.submissionFile, subRequest.parameters, subRequest.files)
            } returns accepted

            testInstance.submit(subRequest)

            coVerify(exactly = 0) { bioWebClient.getSubmissionRequestStatus(any(), any()) }
            coVerify(exactly = 1) {
                create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
                bioWebClient.submitMultipartAsync(subRequest.submissionFile, subRequest.parameters, subRequest.files)
            }
        }

    @Test
    fun `submit with timeout`() =
        runTest {
            val accepted = SubmissionId("S-BSST1", 2)

            coEvery { bioWebClient.getSubmissionRequestStatus("S-BSST1", 2) } returns PROCESSED
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
            coEvery {
                bioWebClient.submitMultipartAsync(subRequest.submissionFile, subRequest.parameters, subRequest.files)
            } returns accepted

            testInstance.submit(subRequest.copy(await = true))

            coVerify(exactly = 1) {
                create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
                bioWebClient.getSubmissionRequestStatus("S-BSST1", 2)
                bioWebClient.submitMultipartAsync(subRequest.submissionFile, subRequest.parameters, subRequest.files)
            }
        }

    @Test
    fun `migrate submission`() =
        runTest {
            val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)

            every { bioWebClient.migrateSubmission(ACC_NO, NFS) } answers { nothing }
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient

            testInstance.migrate(securityConfig, ACC_NO, NFS)

            verify(exactly = 1) {
                bioWebClient.migrateSubmission(ACC_NO, NFS)
                create(SERVER).getAuthenticatedClient(USER, PASSWORD)
            }
        }

    @Test
    fun `transfer submissions to existing user`() =
        runTest {
            val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
            val options = SubmissionTransferOptions(USER, ON_BEHALF, userName = null, listOf(ACC_NO))

            coEvery { bioWebClient.getExtUser(ON_BEHALF) } returns extUser
            coEvery { bioWebClient.transferSubmissions(options) } answers { nothing }
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient

            testInstance.transfer(securityConfig, options)

            coVerify(exactly = 1) {
                bioWebClient.getExtUser(ON_BEHALF)
                bioWebClient.transferSubmissions(options)
                create(SERVER).getAuthenticatedClient(USER, PASSWORD)
            }
        }

    @Test
    fun `transfer submissions to non existing user`() =
        runTest {
            val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
            val options = SubmissionTransferOptions(USER, ON_BEHALF, USER_NAME)

            coEvery { bioWebClient.getExtUser(ON_BEHALF) } throws Exception()
            coEvery { bioWebClient.transferSubmissions(options) } answers { nothing }
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient

            testInstance.transfer(securityConfig, options)

            coVerify(exactly = 1) {
                bioWebClient.getExtUser(ON_BEHALF)
                bioWebClient.transferSubmissions(options)
                create(SERVER).getAuthenticatedClient(USER, PASSWORD)
            }
        }

    @Test
    fun `transfer submissions to non existing user without user name`() =
        runTest {
            val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)
            val options = SubmissionTransferOptions(USER, ON_BEHALF)

            coEvery { bioWebClient.getExtUser(ON_BEHALF) } throws Exception()
            coEvery { bioWebClient.transferSubmissions(options) } answers { nothing }
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient

            val str = "New owner 'onBehalf' doesn't exist. Use option '-un' or '--userName' to provide the full name."
            val error = assertThrows<PrintMessage> { testInstance.transfer(securityConfig, options) }
            assertThat(error).hasMessageContaining(str)

            coVerify(exactly = 1) {
                bioWebClient.getExtUser(ON_BEHALF)
                create(SERVER).getAuthenticatedClient(USER, PASSWORD)
            }
            coVerify(exactly = 0) {
                bioWebClient.transferSubmissions(options)
            }
        }

    @Test
    fun `delete successful`() =
        runTest {
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
            coEvery { bioWebClient.deleteSubmissions(listOf(ACC_NO)) } answers { nothing }

            testInstance.delete(securityConfig, listOf(ACC_NO))

            coVerify(exactly = 1) {
                create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF)
                bioWebClient.deleteSubmissions(listOf(ACC_NO))
            }
        }

    @Test
    fun `perform request throw web client exception with null message`() =
        runTest {
            every { webClientException.message } returns null
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } throws webClientException

            val exception = assertThrows<PrintMessage> { testInstance.delete(securityConfig, listOf(ACC_NO)) }
            assertThat(exception).hasMessageStartingWith("WebClientException: ")
        }

    @Test
    fun `perform request throw web client exception with not null message`() =
        runTest {
            every { webClientException.message } returns ERROR_MESSAGE
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } throws webClientException

            val exception = assertThrows<PrintMessage> { testInstance.delete(securityConfig, listOf(ACC_NO)) }
            assertThat(exception).hasMessage("WebClientException: $ERROR_MESSAGE")
        }

    @Test
    fun `perform request throw other exception with null message`() =
        runTest {
            every { webClientException.message } returns null
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } throws webClientException

            val exception = assertThrows<PrintMessage> { testInstance.delete(securityConfig, listOf(ACC_NO)) }
            assertThat(exception).hasMessageStartingWith("WebClientException: ")
        }

    @Test
    fun `perform request throw other exception with not null message`() =
        runTest {
            every { webClientException.message } returns ERROR_MESSAGE
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } throws webClientException

            val exception = assertThrows<PrintMessage> { testInstance.delete(securityConfig, listOf(ACC_NO)) }
            assertThat(exception).hasMessage("WebClientException: $ERROR_MESSAGE")
        }

    private companion object {
        private const val ACC_NO = "S-BSST0"
        private const val ERROR_MESSAGE = "error message"
        private const val ON_BEHALF = "onBehalf"
        private const val PASSWORD = "password"
        private const val SERVER = "server"
        private const val USER = "user"
        private const val USER_NAME = "userName"

        private val securityConfig = SecurityConfig(SERVER, USER, PASSWORD, ON_BEHALF)
        private val submitParams = SubmitParameters(storageMode = FIRE, preferredSources = listOf(SUBMISSION))
        private val subRequest = SubmissionRequest(mockk(), false, securityConfig, submitParams, listOf(mockk()))
    }
}
