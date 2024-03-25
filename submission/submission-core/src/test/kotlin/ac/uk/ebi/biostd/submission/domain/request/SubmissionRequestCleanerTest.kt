package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.StorageService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
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
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SubmissionRequestCleanerTest(
    @MockK private val storageService: StorageService,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val rqtService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {

    private val testInstance = SubmissionRequestCleaner(
        storageService,
        serializationService,
        eventsPublisherService,
        queryService,
        rqtService,
        filesRequestService,
    )

    @BeforeEach
    fun beforeEach() {
        mockkStatic("uk.ac.ebi.extended.serialization.service.ExtSerializationServiceExtKt")
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `clean common files without current version`(
        @MockK sub: ExtSubmission,
        @MockK loadedRequest: SubmissionRequest,
        @MockK cleanedRequest: SubmissionRequest,
    ) = runTest {

        every { loadedRequest.submission } returns sub
        every { sub.accNo } returns accNo
        every { sub.version } returns version
        every { loadedRequest.withNewStatus(CLEANED) } returns cleanedRequest
        every { eventsPublisherService.requestCleaned(accNo, version) } answers { nothing }

        coEvery { queryService.findExtByAccNo(accNo, true) } returns null
        coEvery {
            rqtService.onRequest(accNo, version, LOADED, processId, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(loadedRequest); }

        testInstance.cleanCurrentVersion(accNo, version, processId)

        coVerify(exactly = 1) { eventsPublisherService.requestCleaned(accNo, version) }
        verify { loadedRequest.withNewStatus(CLEANED) }
        coVerify(exactly = 0) { storageService.deleteSubmissionFile(any(), any()) }
    }

    @Test
    fun `clean common files with current version`(
        @MockK newFile: FireFile,
        @MockK currentFile: FireFile,
        @MockK loadedRequest: SubmissionRequest,
        @MockK cleanedRequest: SubmissionRequest,
        @MockK requestFile: SubmissionRequestFile,
    ) = runTest {
        val new = mockSubmission()
        every { newFile.md5 } returns "new-md5"
        every { newFile.filePath } returns "a/b/file.txt"

        val current = mockSubmission()
        every { requestFile.path } returns "a/b/file.txt"
        every { requestFile.file } returns newFile
        every { currentFile.md5 } returns "current-md5"
        every { currentFile.filePath } returns "a/b/file.txt"

        every { loadedRequest.submission } returns new
        every { loadedRequest.withNewStatus(CLEANED) } returns cleanedRequest

        every { serializationService.filesFlow(current) } returns flowOf(currentFile)
        every { filesRequestService.getSubmissionRequestFiles(accNo, 2, 0) } returns flowOf(requestFile)
        every { eventsPublisherService.requestCleaned(accNo, 2) } answers { nothing }
        coEvery { queryService.findExtByAccNo(accNo, true) } returns current
        coEvery { storageService.deleteSubmissionFile(current, currentFile) } answers { nothing }
        coEvery {
            rqtService.onRequest(accNo, 2, LOADED, processId, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(loadedRequest); }

        testInstance.cleanCurrentVersion(accNo, 2, processId)

        coVerify(exactly = 1) {
            storageService.deleteSubmissionFile(current, currentFile)
            eventsPublisherService.requestCleaned(accNo, 2)
            loadedRequest.withNewStatus(CLEANED)
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
        const val processId = "biostudies-prod"
        const val accNo = "S-BSST1"
        const val version = 1
    }
}
