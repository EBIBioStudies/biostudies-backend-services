package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestReleaser
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

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

    @Nested
    inner class HandleRequest {
        @Test
        fun `when requested`(
            @MockK sub: ExtSubmission
        ) {
            every { requestService.getRequestStatus("accNo", 1) } returns REQUESTED
            every { requestIndexer.indexRequest("accNo", 1) } answers { nothing }
            every { requestLoader.loadRequest("accNo", 1) } returns sub
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
