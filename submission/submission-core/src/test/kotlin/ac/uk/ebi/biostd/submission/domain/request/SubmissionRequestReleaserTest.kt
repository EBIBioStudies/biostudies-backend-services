package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.common.TEST_CONCURRENCY
import ac.uk.ebi.biostd.submission.exceptions.UnreleasedSubmissionException
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.called
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestReleaserTest(
    @MockK private val storageService: FileStorageService,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestReleaser(
        TEST_CONCURRENCY,
        storageService,
        ExtSerializationService(),
        eventsPublisherService,
        queryService,
        persistenceService,
        requestService,
        filesService
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `check released when release`(
        @MockK submission: ExtSubmission,
        @MockK rqt: SubmissionRequest,
        @MockK nfsFile: NfsFile,
        @MockK releasedFile: FireFile,
        @MockK fireFile: FireFile,
    ) = runTest {
        val relPath = "sub-relpath"
        val secretKey = "secret-key"
        val mode = StorageMode.FIRE

        val nfsRqtFile = SubmissionRequestFile(accNo, version, 1, "test1.txt", nfsFile)
        val fireRqtFile = SubmissionRequestFile(accNo, version, 2, "test2.txt", fireFile)

        every { fireFile.published } returns true
        every { rqt.submission } returns submission
        every { rqt.currentIndex } returns 1
        every { submission.accNo } returns accNo
        every { submission.version } returns version
        every { submission.released } returns true
        every { submission.relPath } returns relPath
        every { submission.secretKey } returns secretKey
        every { submission.storageMode } returns mode
        every { eventsPublisherService.requestCheckedRelease(accNo, version) } answers { nothing }
        every { filesService.getSubmissionRequestFiles(accNo, version, 1) } returns flowOf(nfsRqtFile, fireRqtFile)
        coEvery { storageService.releaseSubmissionFile(nfsFile, relPath, secretKey, mode) } returns releasedFile
        every { rqt.withNewStatus(RequestStatus.CHECK_RELEASED) } returns rqt

        coEvery { requestService.updateRqtIndex(nfsRqtFile, releasedFile) } answers { nothing }
        coEvery { requestService.updateRqtIndex(accNo, version, 2) } answers { nothing }
        coEvery {
            requestService.onRequest(accNo, version, FILES_COPIED, processId, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(rqt); }

        testInstance.checkReleased(accNo, version, processId)

        coVerify(exactly = 1) {
            storageService.releaseSubmissionFile(nfsFile, relPath, secretKey, mode)
            eventsPublisherService.requestCheckedRelease(accNo, version)
        }
    }

    @Test
    fun `check released when not released`(
        @MockK rqt: SubmissionRequest,
        @MockK submission: ExtSubmission,
    ) = runTest {
        every { rqt.submission } returns submission
        every { submission.released } returns false
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt
        every { eventsPublisherService.requestCheckedRelease(accNo, version) } answers { nothing }

        coEvery {
            requestService.onRequest(accNo, version, FILES_COPIED, processId, capture(rqtSlot))
        } coAnswers { rqtSlot.captured.invoke(rqt); }

        testInstance.checkReleased(accNo, version, processId)

        verify {
            storageService wasNot called
            persistenceService wasNot called
        }
        coVerify(exactly = 1) {
            eventsPublisherService.requestCheckedRelease(accNo, version)
        }
    }

    @Test
    fun `generate ftp links for private submission`(
        @MockK submission: ExtSubmission,
    ) = runTest {
        every { submission.released } returns false
        coEvery { queryService.getExtByAccNo(accNo, includeFileListFiles = true) } returns submission

        val exception = assertThrows<UnreleasedSubmissionException> { testInstance.generateFtpLinks(accNo) }

        assertThat(exception.message).isEqualTo("Can't generate FTP links for a private submission")
        coVerify {
            storageService wasNot called
        }
    }

    private companion object {
        val accNo = "S-TEST123"
        val version = 1
        const val processId = "biostudies-prod"
        private val rqtSlot = slot<suspend (SubmissionRequest) -> RqtUpdate>()
    }
}
