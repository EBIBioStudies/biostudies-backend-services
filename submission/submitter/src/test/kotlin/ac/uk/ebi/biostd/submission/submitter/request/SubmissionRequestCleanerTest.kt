package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.StorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode.FIRE
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

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
    ) {
        every { loadedRequest.submission } returns sub
        every { queryService.findExtByAccNo("S-BSST1", true) } returns null
        every { loadedRequest.withNewStatus(CLEANED) } returns cleanedRequest
        every { requestService.getLoadedRequest("S-BSST1", 1) } returns loadedRequest
        every { requestService.saveSubmissionRequest(cleanedRequest) } returns ("S-BSST1" to 1)

        testInstance.cleanCurrentVersion("S-BSST1", 1)

        verify(exactly = 1) { requestService.saveSubmissionRequest(cleanedRequest) }
        verify(exactly = 0) {
            storageService.deleteFtpLinks(any())
            storageService.deleteSubmissionFile(any(), any())
            requestService.updateRqtIndex(any(), any(), any())
        }
    }

    @Test
    fun `clean common files with current version`(
        @MockK newFile: ExtFile,
        @MockK currentFile: ExtFile,
        @MockK loadedRequest: SubmissionRequest,
        @MockK cleanedRequest: SubmissionRequest,
        @MockK requestFile: SubmissionRequestFile,
    ) {
        val new = mockSubmission()
        val current = mockSubmission()
        every { requestFile.file } returns newFile
        every { loadedRequest.submission } returns new
        every { newFile.md5 } returns "new-md5"
        every { currentFile.md5 } returns "current-md5"
        every { requestFile.path } returns "a/b/file.txt"
        every { newFile.filePath } returns "a/b/file.txt"
        every { currentFile.filePath } returns "a/b/file.txt"
        every { storageService.deleteFtpLinks(current) } answers { nothing }
        every { loadedRequest.withNewStatus(CLEANED) } returns cleanedRequest
        every { queryService.findExtByAccNo("S-BSST1", true) } returns current
        every { requestService.updateRqtIndex("S-BSST1", 2, 1) } answers { nothing }
        every { requestService.getLoadedRequest("S-BSST1", 2) } returns loadedRequest
        every { serializationService.fileSequence(current) } returns sequenceOf(currentFile)
        every { requestService.saveSubmissionRequest(cleanedRequest) } returns ("S-BSST1" to 2)
        every { storageService.deleteSubmissionFile(current, currentFile) } answers { nothing }
        every { filesRequestService.getSubmissionRequestFiles("S-BSST1", 2, 0) } returns sequenceOf(requestFile)

        testInstance.cleanCurrentVersion("S-BSST1", 2)

        verify(exactly = 1) {
            storageService.deleteFtpLinks(current)
            requestService.updateRqtIndex("S-BSST1", 2, 1)
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
