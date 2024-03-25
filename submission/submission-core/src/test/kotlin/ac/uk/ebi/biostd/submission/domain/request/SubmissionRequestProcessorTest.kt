package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.common.TEST_CONCURRENCY
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestProcessorTest(
    @MockK private val storageService: FileStorageService,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestProcessor(
        TEST_CONCURRENCY,
        storageService,
        eventsPublisherService,
        requestService,
        filesService,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `process request`(
        @MockK submission: ExtSubmission,
        @MockK rqt: SubmissionRequest,
        @MockK nfsFile: NfsFile,
        @MockK releasedFile: FireFile,
    ) = runTest {
        val nfsRqtFile = SubmissionRequestFile(accNo, version, 1, "test1.txt", nfsFile)
        every { rqt.submission } returns submission
        every { rqt.currentIndex } returns 1
        every { submission.accNo } returns accNo
        every { submission.version } returns version
        every { eventsPublisherService.requestFilesCopied(accNo, version) } answers { nothing }
        every { filesService.getSubmissionRequestFiles(accNo, version, 1) } returns flowOf(nfsRqtFile)
        every { rqt.withNewStatus(FILES_COPIED) } returns rqt
        coEvery { storageService.persistSubmissionFile(submission, nfsFile) } returns releasedFile
        coEvery {
            requestService.onRequest(accNo, version, CLEANED, processId, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(rqt); }

        coEvery { requestService.updateRqtIndex(nfsRqtFile, releasedFile) } answers { nothing }

        testInstance.processRequest(accNo, version, processId)

        coVerify(exactly = 1) {
            eventsPublisherService.requestFilesCopied(accNo, version)
        }
    }

    private companion object {
        const val processId = "biostudies-prod"
        const val accNo = "ABC-123"
        const val version = 1
        private val rqtSlot = slot<suspend (SubmissionRequest) -> RqtUpdate>()
    }
}
