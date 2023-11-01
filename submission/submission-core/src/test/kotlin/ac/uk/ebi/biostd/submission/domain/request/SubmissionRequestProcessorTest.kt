package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestProcessorTest(
    @MockK private val storageService: FileStorageService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestProcessor(
        TEST_CONCURRENCY,
        storageService,
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
        val accNo = "ABC-123"
        val version = 1

        val nfsRqtFile = SubmissionRequestFile(accNo, version, 1, "test1.txt", nfsFile)
        every { rqt.submission } returns submission
        every { rqt.currentIndex } returns 1
        every { submission.accNo } returns accNo
        every { submission.version } returns version
        every { filesService.getSubmissionRequestFiles(accNo, version, 1) } returns flowOf(nfsRqtFile)
        coEvery { storageService.persistSubmissionFile(submission, nfsFile) } returns releasedFile
        coEvery { requestService.saveSubmissionRequest(rqt.withNewStatus(FILES_COPIED)) } answers { accNo to version }
        coEvery { requestService.getCleanedRequest(accNo, version) } returns rqt
        coEvery { requestService.updateRqtIndex(nfsRqtFile, releasedFile) } answers { nothing }

        testInstance.processRequest(accNo, version)

        coVerify(exactly = 1) {
            requestService.saveSubmissionRequest(rqt.withNewStatus(FILES_COPIED))
        }
    }
}
