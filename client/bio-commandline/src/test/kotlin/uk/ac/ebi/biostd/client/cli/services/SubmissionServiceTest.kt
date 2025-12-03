package uk.ac.ebi.biostd.client.cli.services

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient.Companion.create
import com.github.ajalt.clikt.core.PrintMessage
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.io.sources.PreferredSource.SUBMISSION
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.SubmissionId
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import uk.ac.ebi.biostd.client.cli.dto.ValidateFileListRequest

@ExtendWith(MockKExtension::class)
internal class SubmissionServiceTest {
    private val testInstance = SubmissionService()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() = mockkObject(SecurityWebClient)

    @Test
    fun `submit`() =
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
    fun `transfer submission`() =
        runTest {
            val securityConfig = SecurityConfig(SERVER, USER, PASSWORD)

            every { bioWebClient.transferSubmission(ACC_NO, NFS) } answers { nothing }
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD) } returns bioWebClient

            testInstance.transfer(securityConfig, ACC_NO, NFS)

            verify(exactly = 1) {
                bioWebClient.transferSubmission(ACC_NO, NFS)
                create(SERVER).getAuthenticatedClient(USER, PASSWORD)
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

    @Test
    fun `validate file list`() =
        runTest {
            val (fileListPath, accNo, rootPath) = validateFileList
            every { create(SERVER).getAuthenticatedClient(USER, PASSWORD, ON_BEHALF) } returns bioWebClient
            coEvery { bioWebClient.validateFileList(fileListPath, rootPath, accNo) } answers { nothing }

            testInstance.validateFileList(validateFileList)

            coVerify(exactly = 1) {
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
        private val bioWebClient: BioWebClient = mockk()
        private val securityConfig = SecurityConfig(SERVER, USER, PASSWORD, ON_BEHALF)
        private val submitParams = SubmitParameters(storageMode = FIRE, preferredSources = listOf(SUBMISSION))

        private val subRequest = SubmissionRequest(mockk(), false, securityConfig, submitParams, listOf(mockk()))
        private val validateFileList = ValidateFileListRequest(FILE_LIST_PATH, ROOT_PATH, ACC_NO, securityConfig)
    }
}
