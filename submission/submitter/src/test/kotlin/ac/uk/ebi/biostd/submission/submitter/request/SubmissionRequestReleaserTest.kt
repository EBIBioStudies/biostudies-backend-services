package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.common.TEST_CONCURRENCY
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
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestReleaserTest(
    @MockK private val storageService: FileStorageService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestReleaser(
        TEST_CONCURRENCY,
        storageService,
        ExtSerializationService(),
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
    ) {
        val accNo = "ABC-123"
        val version = 1
        val relPath = "sub-relpath"
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
        every { submission.storageMode } returns mode
        every { filesService.getSubmissionRequestFiles(accNo, version, 1) } returns flowOf(nfsRqtFile, fireRqtFile)
        coEvery { storageService.releaseSubmissionFile(nfsFile, relPath, mode) } returns releasedFile
        every { requestService.saveSubmissionRequest(rqt.withNewStatus(CHECK_RELEASED)) } answers { accNo to version }
        every { requestService.getFilesCopiedRequest(accNo, version) } returns rqt
        every { requestService.updateRqtIndex(nfsRqtFile, releasedFile) } answers { nothing }
        every { requestService.updateRqtIndex(accNo, version, 2) } answers { nothing }

        testInstance.checkReleased(accNo, version)

        coVerify(exactly = 1) {
            requestService.saveSubmissionRequest(rqt.withNewStatus(CHECK_RELEASED))
            storageService.releaseSubmissionFile(nfsFile, relPath, mode)
        }
    }

    @Test
    fun `check released when not released`(
        @MockK rqt: SubmissionRequest,
        @MockK submission: ExtSubmission,
    ) {
        val accNo = "S-TEST123"
        val version = 1

        every { requestService.getFilesCopiedRequest(accNo, version) } returns rqt
        every { rqt.submission } returns submission
        every { submission.released } returns false
        every { rqt.withNewStatus(CHECK_RELEASED) } returns rqt
        every { requestService.saveSubmissionRequest(rqt.withNewStatus(CHECK_RELEASED)) } answers { accNo to version }

        testInstance.checkReleased("S-TEST123", 1)

        verify {
            storageService wasNot called
            persistenceService wasNot called
        }
        verify(exactly = 1) {
            requestService.saveSubmissionRequest(rqt.withNewStatus(CHECK_RELEASED))
        }
    }
}
