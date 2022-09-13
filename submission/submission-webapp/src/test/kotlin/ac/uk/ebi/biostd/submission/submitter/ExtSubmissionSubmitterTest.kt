package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionReleaser
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.submitter.request.SubmissionRequestProcessor
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ExtSubmissionSubmitterTest(
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestLoader: SubmissionRequestLoader,
    @MockK private val requestProcessor: SubmissionRequestProcessor,
    @MockK private val requestReleaser: SubmissionReleaser,
) {
    private val testInstance = ExtSubmissionSubmitter(
        queryService,
        persistenceService,
        requestLoader,
        requestProcessor,
        requestReleaser
    )

    @Nested
    inner class HandleRequest {
        @Test
        fun `when requested`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns REQUESTED
            every { requestLoader.loadRequest("accNo", 1) } returns sub
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
        }

        @Test
        fun `when loaded`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns LOADED
            every { requestProcessor.processRequest("accNo", 1) } returns sub
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
        }

        @Test
        fun `when files copied`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns FILES_COPIED
            every { requestReleaser.checkReleased("accNo", 1) } returns sub

            val result = testInstance.handleRequest("accNo", 1)

            assertThat(result).isEqualTo(sub)
        }

        @Test
        fun `when already completed`(@MockK sub: ExtSubmission) {
            every { queryService.getRequestStatus("accNo", 1) } returns PROCESSED

            assertThrows<IllegalStateException> { testInstance.handleRequest("accNo", 1) }
        }
    }
}
