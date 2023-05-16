package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence

@ExtendWith(MockKExtension::class)
class SubmissionRequestFinalizerTest(
    @MockK private val storageService: FileStorageService,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
) {
    private val testInstance = SubmissionRequestFinalizer(
        storageService,
        serializationService,
        queryService,
        requestService,
    )

    @BeforeEach
    fun beforeEach() {
        mockkStatic("uk.ac.ebi.extended.serialization.service.ExtSerializationServiceExtKt")
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `delete remaining without previous version`(
        @MockK new: ExtSubmission,
        @MockK persistedRequest: SubmissionRequest,
        @MockK processedRequest: SubmissionRequest,
    ) {
        every { queryService.getExtByAccNo("S-BSST1", true) } returns new
        every { persistedRequest.withNewStatus(PROCESSED) } returns processedRequest
        every { queryService.findLatestInactiveByAccNo("S-BSST1", true) } returns null
        every { requestService.getPersistedRequest("S-BSST1", 1) } returns persistedRequest
        every { requestService.saveSubmissionRequest(processedRequest) } returns ("S-BSST1" to 1)

        testInstance.finalizeRequest("S-BSST1", 1)

        verify(exactly = 1) { requestService.saveSubmissionRequest(processedRequest) }
        verify(exactly = 0) { storageService.deleteSubmissionFile(any(), any()) }
    }

    @Test
    fun `delete remaining from previous version different storage mode`(
        @MockK new: ExtSubmission,
        @MockK previousFile: NfsFile,
        @MockK previous: ExtSubmission,
        @MockK persistedRequest: SubmissionRequest,
        @MockK processedRequest: SubmissionRequest,
    ) {
        every { new.storageMode } returns FIRE
        every { previous.storageMode } returns NFS
        every { previousFile.filePath } returns "a/b/text.txt"
        every { queryService.getExtByAccNo("S-BSST1", true) } returns new
        every { serializationService.fileSequence(new) } returns emptySequence()
        every { persistedRequest.withNewStatus(PROCESSED) } returns processedRequest
        every { queryService.findLatestInactiveByAccNo("S-BSST1", true) } returns previous
        every { requestService.getPersistedRequest("S-BSST1", 2) } returns persistedRequest
        every { serializationService.fileSequence(previous) } returns sequenceOf(previousFile)
        every { storageService.deleteSubmissionFile(previous, previousFile) } answers { nothing }
        every { requestService.saveSubmissionRequest(processedRequest) } returns ("S-BSST1" to 1)

        testInstance.finalizeRequest("S-BSST1", 2)

        verify(exactly = 1) {
            requestService.saveSubmissionRequest(processedRequest)
            storageService.deleteSubmissionFile(previous, previousFile)
        }
    }

    @Test
    fun `delete remaining from previous version`(
        @MockK subFile: FireFile,
        @MockK new: ExtSubmission,
        @MockK previous: ExtSubmission,
        @MockK persistedRequest: SubmissionRequest,
        @MockK processedRequest: SubmissionRequest,
    ) {
        every { new.storageMode } returns FIRE
        every { previous.accNo } returns "S-BSST1"
        every { previous.storageMode } returns FIRE
        every { previous.owner } returns "owner@mail.org"
        every { subFile.filePath } returns "a/b/text.txt"
        every { queryService.getExtByAccNo("S-BSST1", true) } returns new
        every { serializationService.fileSequence(new) } returns emptySequence()
        every { persistedRequest.withNewStatus(PROCESSED) } returns processedRequest
        every { requestService.getPersistedRequest("S-BSST1", 2) } returns persistedRequest
        every { serializationService.fileSequence(previous) } returns sequenceOf(subFile)
        every { queryService.findLatestInactiveByAccNo("S-BSST1", true) } returns previous
        every { storageService.deleteSubmissionFile(previous, subFile) } answers { nothing }
        every { requestService.saveSubmissionRequest(processedRequest) } returns ("S-BSST1" to 1)

        testInstance.finalizeRequest("S-BSST1", 2)

        verify(exactly = 1) {
            storageService.deleteSubmissionFile(previous, subFile)
            requestService.saveSubmissionRequest(processedRequest)
        }
    }
}
