package ac.uk.ebi.biostd.persistence.doc.service

import DefaultSubmission.Companion.ACC_NO
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject.parse
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.FileMode.COPY
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties

@ExtendWith(MockKExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK val subDataRepository: SubmissionDocDataRepository,
    @MockK val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    @MockK val serializationService: ExtSerializationService,
    @MockK val systemService: FileSystemService,
    @MockK val submissionRepository: ExtSubmissionRepository
) {
    private val testInstance = SubmissionMongoPersistenceService(
        subDataRepository,
        submissionRequestDocDataRepository,
        serializationService,
        systemService,
        submissionRepository
    )

    @Nested
    inner class SaveRequest {
        @Test
        fun `save request with current version active`() {
            val subRequestSlot = slot<SubmissionRequest>()
            val current = defaultSubmission(version = 1)
            val newVersion = defaultSubmission(version = 2, status = REQUESTED)
            every { subDataRepository.getCurrentVersion(ACC_NO) } returns 1
            every { serializationService.serialize(newVersion, Properties(true)) } returns "{}"
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } answers { nothing }

            val result = testInstance.saveSubmissionRequest(current)

            assertThat(result).isEqualTo(newVersion)
            val capturedSubRequest = subRequestSlot.captured
            assertThat(capturedSubRequest.accNo).isEqualTo(ACC_NO)
            assertThat(capturedSubRequest.version).isEqualTo(2)
            assertThat(capturedSubRequest.status).isEqualTo(SubmissionRequestStatus.REQUESTED)
            assertThat(capturedSubRequest.submission).isEqualTo(parse("{}"))
        }

        @Test
        fun `save request with current version deleted`() {
            val subRequestSlot = slot<SubmissionRequest>()
            val current = defaultSubmission(version = -1)
            val newVersion = defaultSubmission(version = 2, status = REQUESTED)
            every { subDataRepository.getCurrentVersion(ACC_NO) } returns -1
            every { serializationService.serialize(newVersion, Properties(true)) } returns "{}"
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } answers { nothing }

            val result = testInstance.saveSubmissionRequest(current)

            assertThat(result).isEqualTo(newVersion)
            val capturedSubRequest = subRequestSlot.captured
            assertThat(capturedSubRequest.accNo).isEqualTo(ACC_NO)
            assertThat(capturedSubRequest.version).isEqualTo(2)
            assertThat(capturedSubRequest.status).isEqualTo(SubmissionRequestStatus.REQUESTED)
            assertThat(capturedSubRequest.submission).isEqualTo(parse("{}"))
        }

        @Test
        fun `save request without current version`() {
            val subRequestSlot = slot<SubmissionRequest>()
            val current = defaultSubmission(version = 1)
            val newVersion = defaultSubmission(version = 1, status = REQUESTED)
            every { subDataRepository.getCurrentVersion(ACC_NO) } returns null
            every { serializationService.serialize(newVersion, Properties(true)) } returns "{}"
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } answers { nothing }

            val result = testInstance.saveSubmissionRequest(current)

            assertThat(result).isEqualTo(newVersion)
            val capturedSubRequest = subRequestSlot.captured
            assertThat(capturedSubRequest.accNo).isEqualTo(ACC_NO)
            assertThat(capturedSubRequest.version).isEqualTo(1)
            assertThat(capturedSubRequest.status).isEqualTo(SubmissionRequestStatus.REQUESTED)
            assertThat(capturedSubRequest.submission).isEqualTo(parse("{}"))
        }
    }

    @Test
    fun `processSubmission`() {
        val submission = defaultSubmission()
        val request = SaveSubmissionRequest(submission, COPY, "draftKey")
        val filePersistenceRequest = FilePersistenceRequest(request.submission, request.fileMode)
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns submission
        every { submissionRepository.saveSubmission(submission, request.draftKey) } returns submission

        val result = testInstance.processSubmission(request)

        assertThat(result).isEqualTo(submission)
    }
}
