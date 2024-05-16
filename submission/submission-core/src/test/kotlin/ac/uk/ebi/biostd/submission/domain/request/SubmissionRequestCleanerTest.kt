package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.CONFLICTING
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED_CLEANED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.StorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.StorageMode.FIRE
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SubmissionRequestCleanerTest(
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val storageService: StorageService,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val rqtService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance =
        SubmissionRequestCleaner(
            concurrency = 1,
            queryService,
            storageService,
            eventsPublisherService,
            rqtService,
            filesService,
        )

    @BeforeEach
    fun beforeEach() {
        mockkStatic("uk.ac.ebi.extended.serialization.service.ExtSerializationServiceExtKt")
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun cleanCurrentVersion(
        @MockK sub: ExtSubmission,
        @MockK previousSub: ExtSubmissionInfo,
        @MockK request: SubmissionRequest,
        @MockK cleanedRequest: SubmissionRequest,
        @MockK file: ExtFile,
    ) = runTest {
        every { request.submission } returns sub
        every { request.previousVersion } returns PREVIOUS_VERSION
        every { sub.accNo } returns ACC_NO
        every { sub.version } returns VERSION

        every { request.withNewStatus(CLEANED) } returns cleanedRequest
        every { eventsPublisherService.requestCleaned(ACC_NO, VERSION) } answers { nothing }

        val requestFile = SubmissionRequestFile(ACC_NO, VERSION, 1, "path", file, CONFLICTING)
        every { filesService.getSubmissionRequestFiles(ACC_NO, VERSION, CONFLICTING) } returns flowOf(requestFile)

        coEvery { storageService.deleteSubmissionFile(previousSub, file) } answers { nothing }
        coEvery { queryService.getCoreInfoByAccNoAndVersion(ACC_NO, PREVIOUS_VERSION) } returns previousSub
        coEvery { rqtService.updateRqtFile(requestFile.copy(status = RequestFileStatus.CLEANED)) } answers { nothing }

        coEvery {
            rqtService.onRequest(ACC_NO, VERSION, INDEXED_CLEANED, PROCESS_ID, capture(rqtSlot))
        } coAnswers {
            rqtSlot.captured.invoke(request)
        }

        testInstance.cleanCurrentVersion(ACC_NO, VERSION, PROCESS_ID)

        coVerify(exactly = 1) {
            storageService.deleteSubmissionFile(previousSub, file)
            eventsPublisherService.requestCleaned(ACC_NO, VERSION)
            request.withNewStatus(CLEANED)
            rqtService.updateRqtFile(requestFile.copy(status = RequestFileStatus.CLEANED))
        }
    }

    private fun mockSubmission(): ExtSubmission {
        val mockSubmission = mockk<ExtSubmission>()
        every { mockSubmission.version } returns 2
        every { mockSubmission.accNo } returns "S-BSST1"
        every { mockSubmission.storageMode } returns FIRE
        every { mockSubmission.owner } returns "owner@mail.org"
        return mockSubmission
    }

    private companion object {
        private val rqtSlot = slot<suspend (SubmissionRequest) -> RqtUpdate>()
        const val PROCESS_ID = "biostudies-prod"
        const val ACC_NO = "S-BSST1"
        const val VERSION = 2
        const val PREVIOUS_VERSION = 1
    }
}
