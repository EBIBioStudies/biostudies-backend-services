package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionReleaser
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
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
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val draftService: SubmissionDraftService,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionReleaser,
) {
    private val testInstance = ExtSubmissionSubmitter(
        queryService,
        persistenceService,
        draftService,
        requestLoader,
        requestProcessor,
        requestReleaser,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Nested
    inner class HandleRequest {
        @Test
        fun `when requested`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns REQUESTED
            every { requestLoader.loadRequest("accNo", 1) } returns sub
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub
            every { requestProcessor.cleanCurrentVersion("accNo") } answers { nothing }

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                queryService.getRequestStatus("accNo", 1)
                requestLoader.loadRequest("accNo", 1)
                requestProcessor.cleanCurrentVersion("accNo")
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
        }

        @Test
        fun `when loaded`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns LOADED
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub
            every { requestProcessor.cleanCurrentVersion("accNo") } answers { nothing }

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                queryService.getRequestStatus("accNo", 1)
                requestProcessor.cleanCurrentVersion("accNo")
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
            verify(exactly = 0) {
                requestLoader.loadRequest("accNo", 1)
            }
        }

        @Test
        fun `when cleaned`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns CLEANED
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                queryService.getRequestStatus("accNo", 1)
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
            verify(exactly = 0) {
                requestLoader.loadRequest("accNo", 1)
                requestProcessor.cleanCurrentVersion("accNo")
            }
        }

        @Test
        fun `when files copied`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns FILES_COPIED
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
            verify(exactly = 1) {
                queryService.getRequestStatus("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
            verify(exactly = 0) {
                requestLoader.loadRequest("accNo", 1)
                requestProcessor.cleanCurrentVersion("accNo")
                requestProcessor.processRequest("accNo", 1)
            }
        }

        @Test
        fun `when already completed`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns PROCESSED

            assertThrows<IllegalStateException> { testInstance.handleRequest("accNo", 1) }

            verify(exactly = 1) {
                queryService.getRequestStatus("accNo", 1)
            }
            verify(exactly = 0) {
                requestLoader.loadRequest("accNo", 1)
                requestProcessor.cleanCurrentVersion("accNo")
                requestProcessor.processRequest("accNo", 1)
                requestReleaser.checkReleased("accNo", 1)
            }
        }
    }
}
