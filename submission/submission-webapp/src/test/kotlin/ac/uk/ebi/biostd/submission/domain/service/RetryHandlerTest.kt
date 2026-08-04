package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.model.SubmissionId
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(MockKExtension::class)
class RetryHandlerTest(
    @param:MockK private val extSubmissionService: ExtSubmissionService,
    @param:MockK private val requestService: SubmissionRequestPersistenceService,
) {
    private val testInstance = RetryHandler(extSubmissionService, requestService)

    @BeforeEach
    fun beforeEach() {
        clearMocks(extSubmissionService, requestService)
    }

    @Test
    fun `startup retries stuck requests without active locks`() {
        val locked = SubmissionId("S-BSST1", 1)
        val unlocked = SubmissionId("S-BSST2", 2)
        val candidates = listOf(locked, unlocked)
        every { requestService.getProcessingRequests(Duration.ofHours(3)) } returns flowOf(locked, unlocked)
        coEvery { requestService.getActiveSubmissionLocks(candidates) } returns setOf(locked)
        coEvery { extSubmissionService.reTriggerSubmissionAsync(any()) } returns Unit

        testInstance.onStart()

        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmissionAsync(listOf(unlocked)) }
    }

    @Test
    fun `schedule retries stuck requests without active locks`() {
        val locked = SubmissionId("S-BSST1", 1)
        val unlocked = SubmissionId("S-BSST2", 2)
        val candidates = listOf(locked, unlocked)
        every { requestService.getProcessingRequests(Duration.ofHours(3)) } returns flowOf(locked, unlocked)
        coEvery { requestService.getActiveSubmissionLocks(candidates) } returns setOf(locked)
        coEvery { extSubmissionService.reTriggerSubmissionAsync(any()) } returns Unit

        testInstance.onSchedule()

        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmissionAsync(listOf(unlocked)) }
    }

    @Test
    fun `does not dispatch a retry batch when every stuck request is locked`() {
        val candidates = listOf(SubmissionId("S-BSST1", 1), SubmissionId("S-BSST2", 2))
        every { requestService.getProcessingRequests(Duration.ofHours(3)) } returns flowOf(*candidates.toTypedArray())
        coEvery { requestService.getActiveSubmissionLocks(candidates) } returns candidates.toSet()

        testInstance.onStart()

        coVerify(exactly = 0) { extSubmissionService.reTriggerSubmissionAsync(any()) }
    }

    @Test
    fun `checks active locks and dispatches retries in batches`() {
        val candidates = (1..501).map { SubmissionId("S-BSST$it", 1) }
        val firstBatch = candidates.take(500)
        val secondBatch = candidates.takeLast(1)
        every { requestService.getProcessingRequests(Duration.ofHours(3)) } returns candidates.asFlow()
        coEvery { requestService.getActiveSubmissionLocks(firstBatch) } returns emptySet()
        coEvery { requestService.getActiveSubmissionLocks(secondBatch) } returns emptySet()
        coEvery { extSubmissionService.reTriggerSubmissionAsync(any()) } returns Unit

        testInstance.onStart()

        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmissionAsync(firstBatch) }
        coVerify(exactly = 1) { extSubmissionService.reTriggerSubmissionAsync(secondBatch) }
    }
}
