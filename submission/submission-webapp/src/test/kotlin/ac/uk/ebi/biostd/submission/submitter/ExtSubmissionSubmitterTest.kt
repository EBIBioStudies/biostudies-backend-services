package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
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
internal class ExtSubmissionSubmitterTest(
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestIndexer: SubmissionRequestIndexer,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionRequestReleaser,
    @MockK private val requestCleaner: SubmissionRequestCleaner,
) {
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 1, 2, 3, 4, UTC)
    private val testInstance = ExtSubmissionSubmitter(
        requestService,
        persistenceService,
        requestIndexer,
        requestLoader,
        requestProcessor,
        requestReleaser,
        requestCleaner,
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
        fun `create request`() {
            val submissionRequestSlot = slot<SubmissionRequest>()

            every { persistenceService.getNextVersion("S-TEST123") } returns 2
            every { requestService.createSubmissionRequest(capture(submissionRequestSlot)) } returns ("S-TEST123" to 2)

            testInstance.createRequest(ExtSubmitRequest(basicExtSubmission, "user@test.org", "TMP_123"))
            val request = submissionRequestSlot.captured
            verify(exactly = 1) { requestService.createSubmissionRequest(request) }
            assertThat(request.submission).isEqualTo(basicExtSubmission.copy(version = 2))
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
        fun `when requested`(
            @MockK sub: ExtSubmission
        ) {
            every { requestService.getRequestStatus("accNo", 1) } returns REQUESTED
            every { requestIndexer.indexRequest("accNo", 1) } answers { nothing }
            every { requestLoader.loadRequest("accNo", 1) } answers { nothing }
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub
            every { requestCleaner.cleanCurrentVersion("accNo", 1) } answers { nothing }

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
        }

        @Test
        fun `when loaded`(
            @MockK sub: ExtSubmission
        ) {
            every { requestService.getRequestStatus("accNo", 1) } returns LOADED
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub
            every { requestCleaner.cleanCurrentVersion("accNo", 1) } answers { nothing }

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
            verify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
            }
        }

        @Test
        fun `when cleaned`(
            @MockK sub: ExtSubmission
        ) {
            every { requestService.getRequestStatus("accNo", 1) } returns CLEANED
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
            verify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
            }
        }

        @Test
        fun `when files copied`(
            @MockK sub: ExtSubmission
        ) {
            every { requestService.getRequestStatus("accNo", 1) } returns FILES_COPIED
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
            verify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
            }
        }

        @Test
        fun `when already completed`() {
            every { requestService.getRequestStatus("accNo", 1) } returns PROCESSED

            assertThrows<IllegalStateException> { testInstance.handleRequest("accNo", 1) }

            verify(exactly = 1) {
                requestService.getRequestStatus("accNo", 1)
            }
            verify(exactly = 0) {
                requestIndexer.indexRequest("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestCleaner.cleanCurrentVersion("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
        }
    }
}
