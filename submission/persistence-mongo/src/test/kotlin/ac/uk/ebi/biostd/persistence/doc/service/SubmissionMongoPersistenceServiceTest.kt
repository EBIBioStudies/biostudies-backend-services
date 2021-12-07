package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject.parse
import ebi.ac.uk.extended.model.ExtProcessingStatus
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
import uk.ac.ebi.extended.test.SubmissionFactory.ACC_NO
import uk.ac.ebi.extended.test.SubmissionFactory.defaultSubmission

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

    private companion object {
        const val serializedSub = "{}"
    }

    @Nested
    inner class SaveRequest {
        @Test
        fun `save request with current version active`() {
            val subRequestSlot = slot<SubmissionRequest>()
            val current = defaultSubmission(version = 1)
            val newVersion = defaultSubmission(version = 2, status = ExtProcessingStatus.REQUESTED)
            every { subDataRepository.getCurrentVersion(ACC_NO) } returns 1
            every { serializationService.serialize(newVersion, Properties(true)) } returns serializedSub
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } answers { nothing }

            val result = testInstance.saveSubmissionRequest(current)

            assertThat(result).isEqualTo(newVersion)
            val submissionRequest = SubmissionRequest(ACC_NO, 2, REQUESTED, parse(serializedSub))
            assertThat(subRequestSlot.captured).isEqualToIgnoringGivenFields(submissionRequest, "id")
        }

        @Test
        fun `save request with current version deleted`() {
            val subRequestSlot = slot<SubmissionRequest>()
            val current = defaultSubmission(version = -1)
            val newVersion = defaultSubmission(version = 2, status = ExtProcessingStatus.REQUESTED)
            every { subDataRepository.getCurrentVersion(ACC_NO) } returns -1
            every { serializationService.serialize(newVersion, Properties(true)) } returns serializedSub
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } answers { nothing }

            val result = testInstance.saveSubmissionRequest(current)

            assertThat(result).isEqualTo(newVersion)
            val submissionRequest = SubmissionRequest(ACC_NO, 2, REQUESTED, parse(serializedSub))
            assertThat(subRequestSlot.captured).isEqualToIgnoringGivenFields(submissionRequest, "id")
        }

        @Test
        fun `save request without current version`() {
            val subRequestSlot = slot<SubmissionRequest>()
            val current = defaultSubmission(version = 1)
            val newVersion = defaultSubmission(version = 1, status = ExtProcessingStatus.REQUESTED)
            every { subDataRepository.getCurrentVersion(ACC_NO) } returns null
            every { serializationService.serialize(newVersion, Properties(true)) } returns serializedSub
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } answers { nothing }

            val result = testInstance.saveSubmissionRequest(current)

            assertThat(result).isEqualTo(newVersion)
            val submissionRequest = SubmissionRequest(ACC_NO, 1, REQUESTED, parse(serializedSub))
            assertThat(subRequestSlot.captured).isEqualToIgnoringGivenFields(submissionRequest, "id")
        }
    }

    @Test
    fun `process submission`() {
        val submission = defaultSubmission()
        val request = SaveSubmissionRequest(submission, COPY, "draftKey")
        val filePersistenceRequest = FilePersistenceRequest(request.submission, request.fileMode)
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns submission
        every { submissionRepository.saveSubmission(submission, request.draftKey) } returns submission

        val result = testInstance.processSubmission(request)

        assertThat(result).isEqualTo(submission)
    }
}
