package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StartApplicationHandlerTest(
    @MockK private val extSubmissionService: ExtSubmissionService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
) {

    private val testInstance = StartApplicationHandler(extSubmissionService, requestService)

    @Test
    fun onStart(@MockK submission: ExtSubmission) {
        every { requestService.getProcessingRequests() } returns listOf("a" to 1, "b" to 2)
        every { extSubmissionService.reTriggerSubmission("a", 1) } throws IllegalStateException("Error trigger")
        every { extSubmissionService.reTriggerSubmission("b", 2) } answers { submission }

        testInstance.onStart()

        verify(exactly = 1) { extSubmissionService.reTriggerSubmission("a", 1) }
        verify(exactly = 1) { extSubmissionService.reTriggerSubmission("b", 2) }
    }
}
