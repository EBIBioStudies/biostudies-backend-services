package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SubmissionRequestCleanerTest(
    @MockK private val storageService: StorageService,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestCleaner(
        storageService,
        serializationService,
        queryService,
        requestService,
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
        coEvery { queryService.findExtByAccNo("S-BSST1", true) } returns null
        every { loadedRequest.withNewStatus(CLEANED) } returns cleanedRequest
        coEvery { requestService.getLoadedRequest("S-BSST1", 1) } returns loadedRequest
        coEvery { requestService.saveSubmissionRequest(cleanedRequest) } returns ("S-BSST1" to 1)

        testInstance.cleanCurrentVersion("S-BSST1", 1)

        coVerify(exactly = 1) { requestService.saveSubmissionRequest(cleanedRequest) }
        coVerify(exactly = 0) {
            storageService.deleteSubmissionFile(any(), any())
        }
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
        coEvery { queryService.findExtByAccNo("S-BSST1", true) } returns current
        coEvery { requestService.getLoadedRequest("S-BSST1", 2) } returns loadedRequest
        every { serializationService.filesFlow(current) } returns flowOf(currentFile)
        coEvery { requestService.saveSubmissionRequest(cleanedRequest) } returns ("S-BSST1" to 2)
        coEvery { storageService.deleteSubmissionFile(current, currentFile) } answers { nothing }
        every { filesRequestService.getSubmissionRequestFiles("S-BSST1", 2, 0) } returns flowOf(requestFile)

        testInstance.cleanCurrentVersion("S-BSST1", 2)

        coVerify(exactly = 1) {
            requestService.saveSubmissionRequest(cleanedRequest)
            storageService.deleteSubmissionFile(current, currentFile)
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
}
