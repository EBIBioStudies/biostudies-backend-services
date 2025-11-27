package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.SubmissionId
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.temporal.ChronoUnit.HOURS

@ExtendWith(MockKExtension::class)
class RetryHandlerTest(
    @MockK private val extSubmissionService: ExtSubmissionService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
) {
    private val testInstance = RetryHandler(extSubmissionService, requestService)

    @Test
    fun onStart(
        @MockK submission: ExtSubmission,
    ) {
        every { requestService.getActiveRequests() } returns flowOf(SubmissionId("a", 1), SubmissionId("b", 2))
        coEvery { extSubmissionService.reTriggerSubmission("a", 1) } throws IllegalStateException("Error trigger")
        coEvery { extSubmissionService.reTriggerSubmission("b", 2) } answers { submission }

        testInstance.onStart()

        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmission("a", 1) }
        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmission("b", 2) }
    }

    @Test
    fun onSchedule(
        @MockK submission: ExtSubmission,
    ) {
        every { requestService.getActiveRequests(Duration.of(3, HOURS)) } returns
            flowOf(
                SubmissionId("a", 1),
                SubmissionId("b", 2),
            )
        coEvery { extSubmissionService.reTriggerSubmission("a", 1) } throws IllegalStateException("Error trigger")
        coEvery { extSubmissionService.reTriggerSubmission("b", 2) } answers { submission }

        testInstance.onStart()

        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmission("a", 1) }
        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmission("b", 2) }
    }
}
