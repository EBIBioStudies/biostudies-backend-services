package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.FileMode.COPY
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.test.SubmissionFactory.defaultSubmission

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK val subDataRepository: SubmissionDocDataRepository,
    @MockK val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    @MockK val serializationService: ExtSerializationService,
    @MockK val systemService: FileSystemService,
    @MockK val submissionRepository: ExtSubmissionRepository,
) {
    private val testInstance = SubmissionMongoPersistenceService(
        subDataRepository,
        submissionRequestDocDataRepository,
        serializationService,
        systemService,
        submissionRepository,
    )

    @Test
    fun `process private submission request`() {
        val sub = defaultSubmission()
        val request = SubmissionRequest(sub, COPY, "draftKey")
        val filePersistenceRequest = FilePersistenceRequest(request.submission, request.fileMode)

        every { subDataRepository.release(sub.accNo) } answers { nothing }
        every { submissionRepository.saveSubmission(sub, request.draftKey) } returns sub
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns sub
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns sub
        every { systemService.releaseSubmissionFiles(sub.accNo, sub.owner, sub.relPath) } answers { nothing }
        every { submissionRequestDocDataRepository.updateStatus(PROCESSED, sub.accNo, 1) } answers { nothing }
        every { systemService.unpublishSubmissionFiles(sub.accNo, sub.owner, sub.relPath) } answers { nothing }

        val result = testInstance.processSubmissionRequest(request)

        assertThat(result).isEqualTo(sub)
        verify(exactly = 0) {
            subDataRepository.release(sub.accNo)
            systemService.releaseSubmissionFiles(sub.accNo, sub.owner, sub.relPath)
        }
        verify(exactly = 1) {
            submissionRepository.saveSubmission(sub, request.draftKey)
            systemService.persistSubmissionFiles(filePersistenceRequest)
            systemService.persistSubmissionFiles(filePersistenceRequest)
            submissionRequestDocDataRepository.updateStatus(PROCESSED, sub.accNo, 1)
            systemService.unpublishSubmissionFiles(sub.accNo, sub.owner, sub.relPath)
        }
    }

    @Test
    fun `process public submission request`() {
        val sub = defaultSubmission().copy(released = true)
        val request = SubmissionRequest(sub, COPY, "draftKey")
        val filePersistenceRequest = FilePersistenceRequest(request.submission, request.fileMode)

        every { subDataRepository.release(sub.accNo) } answers { nothing }
        every { submissionRepository.saveSubmission(sub, request.draftKey) } returns sub
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns sub
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns sub
        every { systemService.releaseSubmissionFiles(sub.accNo, sub.owner, sub.relPath) } answers { nothing }
        every { submissionRequestDocDataRepository.updateStatus(PROCESSED, sub.accNo, 1) } answers { nothing }
        every { systemService.unpublishSubmissionFiles(sub.accNo, sub.owner, sub.relPath) } answers { nothing }

        val result = testInstance.processSubmissionRequest(request)

        assertThat(result).isEqualTo(sub)
        verify(exactly = 1) {
            subDataRepository.release(sub.accNo)
            submissionRepository.saveSubmission(sub, request.draftKey)
            systemService.persistSubmissionFiles(filePersistenceRequest)
            systemService.persistSubmissionFiles(filePersistenceRequest)
            systemService.releaseSubmissionFiles(sub.accNo, sub.owner, sub.relPath)
            submissionRequestDocDataRepository.updateStatus(PROCESSED, sub.accNo, 1)
            systemService.unpublishSubmissionFiles(sub.accNo, sub.owner, sub.relPath)
        }
    }
}
