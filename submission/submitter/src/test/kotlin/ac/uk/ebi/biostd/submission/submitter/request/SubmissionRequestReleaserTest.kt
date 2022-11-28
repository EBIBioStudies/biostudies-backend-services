package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionRequestReleaserTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val fileStorageService: FileStorageService,
    @MockK private val eventsPublisherService: EventsPublisherService,
    @MockK private val queryService: SubmissionPersistenceQueryService,
    @MockK private val persistenceService: SubmissionPersistenceService,
    @MockK private val requestService: SubmissionRequestPersistenceService,
    @MockK private val requestFilesService: SubmissionRequestFilesPersistenceService,
) {
    private val testInstance = SubmissionRequestReleaser(
        fileStorageService,
        ExtSerializationService(),
        eventsPublisherService,
        queryService,
        persistenceService,
        requestService,
        requestFilesService
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `check released`(
        @MockK request: SubmissionRequest,
        @MockK releasedFile: ExtFile,
    ) {
        val nfsFile = createNfsFile("public.txt", "Files/public.txt", temporaryFolder.createFile("public.txt"))
        val sub = basicExtSubmission.copy(released = true)
        val toPublishFile = SubmissionRequestFile(sub.accNo, sub.version, 0, "test.txt", nfsFile)
        val expectedFile = SubmissionRequestFile(sub.accNo, sub.version, 0, "test.txt", releasedFile)

        every { request.notifyTo } returns "user@test.org"
        every { request.submission } returns sub
        every { request.currentIndex } returns 0
        every { persistenceService.setAsReleased("S-TEST123") } answers { nothing }
        every { requestService.getFilesCopiedRequest("S-TEST123", 1) } returns request
        every { requestService.saveSubmissionRequest(request.withNewStatus(PROCESSED)) } answers { "S-TEST123" to 1 }
        every { requestFilesService.getSubmissionRequestFiles("S-TEST123", 1, 0) } returns sequenceOf(toPublishFile)
        every { requestService.updateRequestFile(expectedFile) } answers { nothing }
        every { eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org") } answers { nothing }
        every {
            fileStorageService.releaseSubmissionFile(
                nfsFile,
                sub.relPath,
                sub.storageMode
            )
        } answers { releasedFile }

        testInstance.checkReleased("S-TEST123", 1)

        verify(exactly = 1) {
            persistenceService.setAsReleased("S-TEST123")
            requestService.saveSubmissionRequest(request.withNewStatus(PROCESSED))
            eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org")
            fileStorageService.releaseSubmissionFile(nfsFile, sub.relPath, sub.storageMode)
        }
    }

    @Test
    fun `check released when not released`(
        @MockK request: SubmissionRequest,
    ) {
        val sub = basicExtSubmission.copy(released = false)

        every { request.notifyTo } returns "user@test.org"
        every { request.submission } returns sub
        every { request.currentIndex } returns 0
        every { persistenceService.setAsReleased("S-TEST123") } answers { nothing }
        every { requestService.getFilesCopiedRequest("S-TEST123", 1) } returns request
        every { requestService.saveSubmissionRequest(request.withNewStatus(PROCESSED)) } answers { "S-TEST123" to 1 }
        every { eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org") } answers { nothing }

        testInstance.checkReleased("S-TEST123", 1)

        verify(exactly = 0) {
            persistenceService.setAsReleased("S-TEST123")
            fileStorageService.releaseSubmissionFile(any(), sub.relPath, sub.storageMode)
        }
        verify(exactly = 1) {
            requestService.saveSubmissionRequest(request.withNewStatus(PROCESSED))
            eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org")
        }
    }
}
