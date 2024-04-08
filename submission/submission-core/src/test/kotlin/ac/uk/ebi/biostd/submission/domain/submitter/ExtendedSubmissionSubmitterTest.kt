package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.SubmissionTaskProperties
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.asserts.assertThrows
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ExtendedSubmissionSubmitterTest(
    @MockK private val submission: ExtSubmission,
    @MockK private val localExtSubmissionSubmitter: LocalExtSubmissionSubmitter,
    @MockK private val remoteExtSubmissionSubmitter: RemoteExtSubmissionSubmitter,
    @MockK private val submissionTaskProperties: SubmissionTaskProperties,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
) {
    private val testInstance =
        ExtendedSubmissionSubmitter(
            localExtSubmissionSubmitter,
            remoteExtSubmissionSubmitter,
            submissionTaskProperties,
            requestService,
            queryService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        setUpLocalSubmitter()
        setUpRemoteSubmitter()
    }

    @Nested
    inner class LocalExecution {
        @BeforeEach
        fun beforeEach() {
            every { submissionTaskProperties.enabled } returns false
        }

        @Test
        fun `handle request`() =
            runTest {
                every { submissionTaskProperties.enabled } returns false
                coEvery { localExtSubmissionSubmitter.handleRequest(ACC_NO, VERSION) } returns submission

                testInstance.handleRequest(ACC_NO, VERSION)
                coVerify(exactly = 1) { localExtSubmissionSubmitter.handleRequest(ACC_NO, VERSION) }
            }
    }

    @Nested
    inner class RemoteExecution {
        @BeforeEach
        fun beforeEach() {
            every { submissionTaskProperties.enabled } returns true
            coEvery { queryService.getExtByAccNo(ACC_NO) } returns submission
            coEvery { queryService.existByAccNoAndVersion(ACC_NO, VERSION) } returns true
        }

        @Test
        fun `load request`() =
            runTest {
                testInstance.loadRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { remoteExtSubmissionSubmitter.loadRequest(ACC_NO, VERSION) }
                coVerify(exactly = 0) { localExtSubmissionSubmitter.loadRequest(any(), any()) }
            }

        @Test
        fun `clean request`() =
            runTest {
                testInstance.cleanRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { remoteExtSubmissionSubmitter.cleanRequest(ACC_NO, VERSION) }
                coVerify(exactly = 0) { localExtSubmissionSubmitter.cleanRequest(any(), any()) }
            }

        @Test
        fun `process request`() =
            runTest {
                testInstance.processRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { remoteExtSubmissionSubmitter.processRequest(ACC_NO, VERSION) }
                coVerify(exactly = 0) { localExtSubmissionSubmitter.processRequest(any(), any()) }
            }

        @Test
        fun `check released request`() =
            runTest {
                testInstance.checkReleased(ACC_NO, VERSION)

                coVerify(exactly = 1) { remoteExtSubmissionSubmitter.checkReleased(ACC_NO, VERSION) }
                coVerify(exactly = 0) { localExtSubmissionSubmitter.checkReleased(any(), any()) }
            }

        @Test
        fun `handle complete request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns REQUESTED

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    localExtSubmissionSubmitter.indexRequest(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.loadRequest(any(), any())
                    localExtSubmissionSubmitter.cleanRequest(any(), any())
                    localExtSubmissionSubmitter.processRequest(any(), any())
                    localExtSubmissionSubmitter.checkReleased(any(), any())

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle indexed request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns INDEXED

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    remoteExtSubmissionSubmitter.loadRequest(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.indexRequest(any(), any())
                    localExtSubmissionSubmitter.loadRequest(any(), any())
                    localExtSubmissionSubmitter.cleanRequest(any(), any())
                    localExtSubmissionSubmitter.processRequest(any(), any())
                    localExtSubmissionSubmitter.checkReleased(any(), any())

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle loaded request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns LOADED

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    remoteExtSubmissionSubmitter.cleanRequest(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.indexRequest(any(), any())
                    localExtSubmissionSubmitter.loadRequest(any(), any())
                    localExtSubmissionSubmitter.cleanRequest(any(), any())

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.loadRequest(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle cleaned request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns CLEANED

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    remoteExtSubmissionSubmitter.processRequest(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.indexRequest(any(), any())
                    localExtSubmissionSubmitter.loadRequest(any(), any())
                    localExtSubmissionSubmitter.cleanRequest(any(), any())
                    localExtSubmissionSubmitter.processRequest(any(), any())

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.loadRequest(any(), any())
                    remoteExtSubmissionSubmitter.cleanRequest(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle files copied request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns FILES_COPIED

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    remoteExtSubmissionSubmitter.checkReleased(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.indexRequest(any(), any())
                    localExtSubmissionSubmitter.loadRequest(any(), any())
                    localExtSubmissionSubmitter.cleanRequest(any(), any())
                    localExtSubmissionSubmitter.processRequest(any(), any())
                    localExtSubmissionSubmitter.checkReleased(any(), any())

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.loadRequest(any(), any())
                    remoteExtSubmissionSubmitter.cleanRequest(any(), any())
                    remoteExtSubmissionSubmitter.processRequest(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle released request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns CHECK_RELEASED
                coEvery { localExtSubmissionSubmitter.saveAndFinalize(ACC_NO, VERSION) } returns submission

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    localExtSubmissionSubmitter.saveAndFinalize(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.indexRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.loadRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.cleanRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.processRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.checkReleased(ACC_NO, VERSION)

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.loadRequest(any(), any())
                    remoteExtSubmissionSubmitter.cleanRequest(any(), any())
                    remoteExtSubmissionSubmitter.processRequest(any(), any())
                    remoteExtSubmissionSubmitter.checkReleased(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle persisted request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns PERSISTED

                testInstance.handleRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) {
                    localExtSubmissionSubmitter.finalizeRequest(ACC_NO, VERSION)
                }

                coVerify(exactly = 0) {
                    localExtSubmissionSubmitter.indexRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.loadRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.cleanRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.processRequest(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.checkReleased(ACC_NO, VERSION)
                    localExtSubmissionSubmitter.saveRequest(ACC_NO, VERSION)

                    remoteExtSubmissionSubmitter.indexRequest(any(), any())
                    remoteExtSubmissionSubmitter.loadRequest(any(), any())
                    remoteExtSubmissionSubmitter.cleanRequest(any(), any())
                    remoteExtSubmissionSubmitter.processRequest(any(), any())
                    remoteExtSubmissionSubmitter.checkReleased(any(), any())
                    remoteExtSubmissionSubmitter.saveRequest(any(), any())
                    remoteExtSubmissionSubmitter.finalizeRequest(any(), any())
                }
            }

        @Test
        fun `handle processed request`() =
            runTest {
                coEvery { requestService.getRequestStatus(ACC_NO, VERSION) } returns PROCESSED

                val exception = assertThrows<IllegalStateException> { testInstance.handleRequest(ACC_NO, VERSION) }
                assertThat(exception.message).isEqualTo("Request accNo=S-BSST1, version=1 has been already processed")
            }
    }

    private fun setUpLocalSubmitter() {
        coEvery { localExtSubmissionSubmitter.indexRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { localExtSubmissionSubmitter.loadRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { localExtSubmissionSubmitter.cleanRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { localExtSubmissionSubmitter.processRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { localExtSubmissionSubmitter.checkReleased(ACC_NO, VERSION) } answers { nothing }
        coEvery { localExtSubmissionSubmitter.saveRequest(ACC_NO, VERSION) } returns submission
        coEvery { localExtSubmissionSubmitter.finalizeRequest(ACC_NO, VERSION) } returns submission
    }

    private fun setUpRemoteSubmitter() {
        coEvery { remoteExtSubmissionSubmitter.loadRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { remoteExtSubmissionSubmitter.cleanRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { remoteExtSubmissionSubmitter.processRequest(ACC_NO, VERSION) } answers { nothing }
        coEvery { remoteExtSubmissionSubmitter.checkReleased(ACC_NO, VERSION) } answers { nothing }
    }

    private companion object {
        const val ACC_NO = "S-BSST1"
        const val VERSION = 1
    }
}
