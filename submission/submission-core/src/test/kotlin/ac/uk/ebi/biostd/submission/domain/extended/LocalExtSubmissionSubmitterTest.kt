package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleanIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestSaver
import ac.uk.ebi.biostd.submission.domain.submitter.LocalExtSubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.CLEANED
import ebi.ac.uk.model.RequestStatus.FILES_COPIED
import ebi.ac.uk.model.RequestStatus.LOADED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import ebi.ac.uk.model.RequestStatus.REQUESTED
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class LocalExtSubmissionSubmitterTest(
    @MockK private val properties: ApplicationProperties,
    @MockK private val sub: ExtSubmission,
    @MockK private val pageTabService: PageTabService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestIndexer: SubmissionRequestIndexer,
    @MockK private val requestCleanIndexer: SubmissionRequestCleanIndexer,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionRequestReleaser,
    @MockK private val requestCleaner: SubmissionRequestCleaner,
    @MockK private val requestSaver: SubmissionRequestSaver,
    @MockK private val subQueryService: ExtSubmissionQueryService,
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, UTC)
    private val testInstance =
        LocalExtSubmissionSubmitter(
            properties,
            pageTabService,
            requestService,
            persistenceService,
            requestIndexer,
            requestCleanIndexer,
            requestLoader,
            requestProcessor,
            requestReleaser,
            requestCleaner,
            requestSaver,
            subQueryService,
        )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { properties.processId } returns INSTANCE_ID
        every { OffsetDateTime.now() } returns mockNow
    }

    @Nested
    inner class CreateRequest {
        @Test
        fun `create request`() =
            runTest {
                val submission = basicExtSubmission
                val submissionRequestSlot = slot<SubmissionRequest>()

                coEvery { persistenceService.getNextVersion("S-TEST123") } returns 2
                coEvery { pageTabService.generatePageTab(submission) } returns submission
                coEvery { requestService.createRequest(capture(submissionRequestSlot)) } returns ("S-TEST123" to 2)

                testInstance.createRequest(ExtSubmitRequest(submission, "user@test.org", "TMP_123"))

                val request = submissionRequestSlot.captured
                coVerify(exactly = 1) {
                    pageTabService.generatePageTab(submission)
                    requestService.createRequest(request)
                }
                assertThat(request.submission).isEqualTo(submission.copy(version = 2))
                assertThat(request.draftKey).isEqualTo("TMP_123")
                assertThat(request.notifyTo).isEqualTo("user@test.org")
                assertThat(request.status).isEqualTo(REQUESTED)
                assertThat(request.totalFiles).isEqualTo(0)
                assertThat(request.currentIndex).isEqualTo(0)
                assertThat(request.modificationTime).isEqualTo(mockNow)
            }
    }

    @Nested
    inner class RequestStages {
        @Test
        fun `index request`() =
            runTest {
                coEvery { requestIndexer.indexRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

                testInstance.indexRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { requestIndexer.indexRequest(ACC_NO, VERSION, INSTANCE_ID) }
            }

        @Test
        fun `load request`() =
            runTest {
                coEvery { requestLoader.loadRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

                testInstance.loadRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { requestLoader.loadRequest(ACC_NO, VERSION, INSTANCE_ID) }
            }

        @Test
        fun `clean request`() =
            runTest {
                coEvery { requestCleaner.cleanCurrentVersion(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

                testInstance.cleanRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { requestCleaner.cleanCurrentVersion(ACC_NO, VERSION, INSTANCE_ID) }
            }

        @Test
        fun `process request`() =
            runTest {
                coEvery { requestProcessor.processRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

                testInstance.processRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { requestProcessor.processRequest(ACC_NO, VERSION, INSTANCE_ID) }
            }

        @Test
        fun `check released`() =
            runTest {
                coEvery { requestReleaser.checkReleased(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }

                testInstance.checkReleased(ACC_NO, VERSION)

                coVerify(exactly = 1) { requestReleaser.checkReleased(ACC_NO, VERSION, INSTANCE_ID) }
            }

        @Test
        fun `save request`() =
            runTest {
                coEvery { requestSaver.saveRequest(ACC_NO, VERSION, INSTANCE_ID) } returns sub

                testInstance.saveRequest(ACC_NO, VERSION)

                coVerify(exactly = 1) { requestSaver.saveRequest(ACC_NO, VERSION, INSTANCE_ID) }
            }

        @Test
        fun `finalize request`() =
            runTest {
                coEvery { requestCleaner.finalizeRequest(ACC_NO, VERSION, INSTANCE_ID) } answers { nothing }
                coEvery { subQueryService.getExtendedSubmission(ACC_NO, includeFileListFiles = false) } returns sub

                val result = testInstance.finalizeRequest(ACC_NO, VERSION)

                assertThat(result).isEqualTo(sub)
                coVerify(exactly = 1) { requestCleaner.finalizeRequest(ACC_NO, VERSION, INSTANCE_ID) }
            }
    }

    @Nested
    inner class HandleRequest {
        @Test
        fun `when requested`() =
            runTest {
                coEvery { requestService.getRequestStatus("accNo", 1) } returns REQUESTED
                coEvery { requestIndexer.indexRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestLoader.loadRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestCleanIndexer.indexRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestProcessor.processRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestReleaser.checkReleased("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestSaver.saveRequest("accNo", 1, INSTANCE_ID) } answers { sub }
                coEvery { requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { subQueryService.getExtendedSubmission("accNo", includeFileListFiles = false) } returns sub

                val result = testInstance.handleRequest("accNo", 1)

                assertThat(result).isEqualTo(sub)
                coVerify(exactly = 1) {
                    requestService.getRequestStatus("accNo", 1)
                    requestIndexer.indexRequest("accNo", 1, INSTANCE_ID)
                    requestLoader.loadRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID)
                    requestProcessor.processRequest("accNo", 1, INSTANCE_ID)
                    requestReleaser.checkReleased("accNo", 1, INSTANCE_ID)
                    requestSaver.saveRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID)
                }
            }

        @Test
        fun `when loaded`() =
            runTest {
                coEvery { requestService.getRequestStatus("accNo", 1) } returns LOADED
                coEvery { requestCleanIndexer.indexRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestProcessor.processRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestReleaser.checkReleased("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestSaver.saveRequest("accNo", 1, INSTANCE_ID) } answers { sub }
                coEvery { requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { subQueryService.getExtendedSubmission("accNo", includeFileListFiles = false) } returns sub

                val result = testInstance.handleRequest("accNo", 1)

                assertThat(result).isEqualTo(sub)
                coVerify(exactly = 1) {
                    requestService.getRequestStatus("accNo", 1)
                    requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID)
                    requestProcessor.processRequest("accNo", 1, INSTANCE_ID)
                    requestReleaser.checkReleased("accNo", 1, INSTANCE_ID)
                    requestSaver.saveRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID)
                }
                coVerify(exactly = 0) {
                    requestIndexer.indexRequest("accNo", 1, INSTANCE_ID)
                    requestLoader.loadRequest("accNo", 1, INSTANCE_ID)
                }
            }

        @Test
        fun `when cleaned`() =
            runTest {
                coEvery { requestService.getRequestStatus("accNo", 1) } returns CLEANED
                coEvery { requestProcessor.processRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestReleaser.checkReleased("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestSaver.saveRequest("accNo", 1, INSTANCE_ID) } answers { sub }
                coEvery { requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { subQueryService.getExtendedSubmission("accNo") } returns sub

                val result = testInstance.handleRequest("accNo", 1)

                assertThat(result).isEqualTo(sub)
                coVerify(exactly = 1) {
                    requestService.getRequestStatus("accNo", 1)
                    requestProcessor.processRequest("accNo", 1, INSTANCE_ID)
                    requestReleaser.checkReleased("accNo", 1, INSTANCE_ID)
                    requestSaver.saveRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID)
                }
                coVerify(exactly = 0) {
                    requestIndexer.indexRequest("accNo", 1, INSTANCE_ID)
                    requestLoader.loadRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID)
                }
            }

        @Test
        fun `when files copied`() =
            runTest {
                coEvery { requestService.getRequestStatus("accNo", 1) } returns FILES_COPIED
                coEvery { requestReleaser.checkReleased("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestSaver.saveRequest("accNo", 1, INSTANCE_ID) } answers { sub }
                coEvery { requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { subQueryService.getExtendedSubmission("accNo", includeFileListFiles = false) } returns sub

                val result = testInstance.handleRequest("accNo", 1)

                assertThat(result).isEqualTo(sub)
                coVerify(exactly = 1) {
                    requestService.getRequestStatus("accNo", 1)
                    requestReleaser.checkReleased("accNo", 1, INSTANCE_ID)
                    requestSaver.saveRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID)
                }
                coVerify(exactly = 0) {
                    requestIndexer.indexRequest("accNo", 1, INSTANCE_ID)
                    requestLoader.loadRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID)
                    requestProcessor.processRequest("accNo", 1, INSTANCE_ID)
                }
            }

        @Test
        fun `when checked released`() =
            runTest {
                coEvery { requestService.getRequestStatus("accNo", 1) } returns CHECK_RELEASED
                coEvery { requestSaver.saveRequest("accNo", 1, INSTANCE_ID) } returns sub
                coEvery { requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { subQueryService.getExtendedSubmission("accNo", includeFileListFiles = false) } returns sub

                val result = testInstance.handleRequest("accNo", 1)

                assertThat(result).isEqualTo(sub)
                coVerify(exactly = 1) {
                    requestService.getRequestStatus("accNo", 1)
                    requestSaver.saveRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID)
                }
                coVerify(exactly = 0) {
                    requestIndexer.indexRequest("accNo", 1, INSTANCE_ID)
                    requestLoader.loadRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID)
                    requestProcessor.processRequest("accNo", 1, INSTANCE_ID)
                    requestReleaser.checkReleased("accNo", 1, INSTANCE_ID)
                }
            }

        @Test
        fun `when persisted`() =
            runTest {
                coEvery { requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID) } answers { nothing }
                coEvery { requestService.getRequestStatus("accNo", 1) } returns PERSISTED
                coEvery { subQueryService.getExtendedSubmission("accNo", includeFileListFiles = false) } returns sub

                testInstance.handleRequest("accNo", 1)

                coVerify(exactly = 1) {
                    requestService.getRequestStatus("accNo", 1)
                    requestCleaner.finalizeRequest("accNo", 1, INSTANCE_ID)
                }
                coVerify(exactly = 0) {
                    requestIndexer.indexRequest("accNo", 1, INSTANCE_ID)
                    requestLoader.loadRequest("accNo", 1, INSTANCE_ID)
                    requestCleaner.cleanCurrentVersion("accNo", 1, INSTANCE_ID)
                    requestProcessor.processRequest("accNo", 1, INSTANCE_ID)
                    requestReleaser.checkReleased("accNo", 1, INSTANCE_ID)
                    requestSaver.saveRequest("accNo", 1, INSTANCE_ID)
                }
            }
    }

    private companion object {
        const val ACC_NO = "S-BSST1"
        const val INSTANCE_ID = "biostudies-prod"
        const val VERSION = 1
    }
}
