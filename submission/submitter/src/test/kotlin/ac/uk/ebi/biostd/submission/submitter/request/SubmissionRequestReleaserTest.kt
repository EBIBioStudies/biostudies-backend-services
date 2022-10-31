package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtSection
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
) {
    private val testInstance = SubmissionRequestReleaser(
        fileStorageService,
        ExtSerializationService(),
        eventsPublisherService,
        queryService,
        persistenceService,
        requestService,
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `check released`(
        @MockK request: SubmissionRequest
    ) {
        val file = createNfsFile("public.txt", "Files/public.txt", temporaryFolder.createFile("public.txt"))
        val sub = basicExtSubmission.copy(
            released = true,
            section = ExtSection(type = "Exp", files = listOf(left(file))),
        )

        every { request.notifyTo } returns "user@test.org"
        every { persistenceService.setAsReleased("S-TEST123") } answers { nothing }
        every { requestService.getFilesCopiedRequest("S-TEST123", 1) } returns request
        every { requestService.updateRequestIndex("S-TEST123", 1, 0) } answers { nothing }
        every { requestService.updateRequestStatus("S-TEST123", 1, PROCESSED) } answers { nothing }
        every { eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org") } answers { nothing }
        every { queryService.getExtByAccNoAndVersion("S-TEST123", 1, includeFileListFiles = true) } returns sub
        every { fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode) } answers { nothing }

        testInstance.checkReleased("S-TEST123", 1)

        verify(exactly = 1) {
            persistenceService.setAsReleased("S-TEST123")
            requestService.updateRequestIndex("S-TEST123", 1, 0)
            requestService.updateRequestStatus("S-TEST123", 1, PROCESSED)
            eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org")
            fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode)
        }
    }

    @Test
    fun `check released when not released`(
        @MockK request: SubmissionRequest
    ) {
        val file = createNfsFile("private.txt", "Files/private.txt", temporaryFolder.createFile("private.txt"))
        val sub = basicExtSubmission.copy(
            released = false,
            section = ExtSection(type = "Exp", files = listOf(left(file))),
        )

        every { request.notifyTo } returns "user@test.org"
        every { persistenceService.setAsReleased("S-TEST123") } answers { nothing }
        every { requestService.getFilesCopiedRequest("S-TEST123", 1) } returns request
        every { requestService.updateRequestIndex("S-TEST123", 1, 0) } answers { nothing }
        every { requestService.updateRequestStatus("S-TEST123", 1, PROCESSED) } answers { nothing }
        every { eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org") } answers { nothing }
        every { queryService.getExtByAccNoAndVersion("S-TEST123", 1, includeFileListFiles = true) } returns sub
        every { fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode) } answers { nothing }

        testInstance.checkReleased("S-TEST123", 1)

        verify(exactly = 0) {
            persistenceService.setAsReleased("S-TEST123")
            requestService.updateRequestIndex("S-TEST123", 1, 0)
            fileStorageService.releaseSubmissionFile(file, sub.relPath, sub.storageMode)
        }
        verify(exactly = 1) {
            requestService.updateRequestStatus("S-TEST123", 1, PROCESSED)
            eventsPublisherService.submissionSubmitted("S-TEST123", "user@test.org")
        }
    }
}
