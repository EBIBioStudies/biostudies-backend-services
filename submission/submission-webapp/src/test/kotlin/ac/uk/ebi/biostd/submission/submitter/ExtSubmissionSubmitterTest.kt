package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestFinalizer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestSaver
import ebi.ac.uk.extended.model.ExtSubmission
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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class ExtSubmissionSubmitterTest(
    @MockK private val sub: ExtSubmission,
    @MockK private val pageTabService: PageTabService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestIndexer: SubmissionRequestIndexer,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionRequestReleaser,
    @MockK private val requestCleaner: SubmissionRequestCleaner,
    @MockK private val requestSaver: SubmissionRequestSaver,
    @MockK private val requestFinalizer: SubmissionRequestFinalizer,
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, UTC)
    private val testInstance = ExtSubmissionSubmitter(
        pageTabService,
        requestService,
        persistenceService,
        requestIndexer,
        requestLoader,
        requestProcessor,
        requestReleaser,
        requestCleaner,
        requestSaver,
        requestFinalizer,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
    }

    @Nested
    inner class CreateRequest {
        @Test
        fun `create request`() = runTest {
            val submission = basicExtSubmission
            val submissionRequestSlot = slot<SubmissionRequest>()

            coEvery { persistenceService.getNextVersion("S-TEST123") } returns 2
            coEvery { pageTabService.generatePageTab(submission) } returns submission
            coEvery { requestService.createSubmissionRequest(capture(submissionRequestSlot)) } returns ("S-TEST123" to 2)

            testInstance.createRequest(ExtSubmitRequest(submission, "user@test.org", "TMP_123"))

            val request = submissionRequestSlot.captured
            coVerify(exactly = 1) {
                pageTabService.generatePageTab(submission)
                requestService.createSubmissionRequest(request)
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
    inner class HandleRequest {
        @Test
        fun `when requested`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns REQUESTED
            coEvery { requestIndexer.indexRequest("accNo", 1) } answers { nothing }
            coEvery { requestLoader.loadRequest("accNo", 1) } answers { nothing }
            coEvery { requestProcessor.processRequest("accNo", 1) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1) } answers { nothing }
            coEvery { requestCleaner.cleanCurrentVersion("accNo", 1) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
                requestSaver.saveRequest("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1)
            }
        }

        @Test
        fun `when loaded`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns LOADED
            coEvery { requestProcessor.processRequest("accNo", 1) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1) } answers { nothing }
            coEvery { requestCleaner.cleanCurrentVersion("accNo", 1) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
                requestSaver.saveRequest("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
            }
        }

        @Test
        fun `when cleaned`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns CLEANED
            coEvery { requestProcessor.processRequest("accNo", 1) } answers { nothing }
            coEvery { requestReleaser.checkReleased("accNo", 1) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
                requestSaver.saveRequest("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
            }
        }

        @Test
        fun `when files copied`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns FILES_COPIED
            coEvery { requestReleaser.checkReleased("accNo", 1) } answers { nothing }
            coEvery { requestSaver.saveRequest("accNo", 1) } answers { sub }
            coEvery { requestFinalizer.finalizeRequest("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
                requestSaver.saveRequest("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
            }
        }

        @Test
        fun `when checked released`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns CHECK_RELEASED
            coEvery { requestSaver.saveRequest("accNo", 1) } returns sub
            coEvery { requestFinalizer.finalizeRequest("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestSaver.saveRequest("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
        }

        @Test
        fun `when persisted`() = runTest {
            coEvery { requestFinalizer.finalizeRequest("accNo", 1) } returns sub
            coEvery { requestService.getRequestStatus("accNo", 1) } returns PERSISTED

            testInstance.handleRequest("accNo", 1)

            coVerify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestFinalizer.finalizeRequest("accNo", 1)
            }
            coVerify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
                requestSaver.saveRequest("accNo", 1)
            }
        }

        @Test
        fun `when processed`() = runTest {
            coEvery { requestService.getRequestStatus("accNo", 1) } returns PROCESSED

            val exception = assertThrows<IllegalStateException> { testInstance.handleRequest("accNo", 1) }
            assertThat(exception.message).isEqualTo("Request accNo=accNo, version='1' has been already processed")
        }
    }
}
