package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import arrow.core.Either.Companion.left
import com.mongodb.BasicDBObject.parse
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.FileMode.COPY
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import uk.ac.ebi.extended.test.FileListFactory.defaultFileList
import uk.ac.ebi.extended.test.SectionFactory.defaultSection
import uk.ac.ebi.extended.test.SubmissionFactory.ACC_NO
import uk.ac.ebi.extended.test.SubmissionFactory.defaultSubmission
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK val subDataRepository: SubmissionDocDataRepository,
    @MockK val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    @MockK val serializationService: ExtSerializationService,
    @MockK val systemService: FileSystemService,
    @MockK val submissionRepository: ExtSubmissionRepository,
    private val temporaryFolder: TemporaryFolder
) {
    @OptIn(ExperimentalPathApi::class)
    private val testInstance = SubmissionMongoPersistenceService(
        subDataRepository,
        submissionRequestDocDataRepository,
        serializationService,
        systemService,
        submissionRepository,
        temporaryFolder.root.toPath()
    )

    private companion object {
        const val serializedSub = "{}"
    }

    @Nested
    inner class SaveRequest {
        @Test
        fun `save request with current version active`() {
            val subRequestSlot = slot<DocSubmissionRequest>()
            val subSection = defaultSection(fileList = defaultFileList())
            val section = defaultSection(fileList = defaultFileList(), sections = listOf(left(subSection)))
            val newVersion = defaultSubmission(version = 2, status = ExtProcessingStatus.REQUESTED, section = section)
            val request = SubmissionRequest(defaultSubmission(version = 1), COPY, "draftKey")

            every { subDataRepository.getCurrentVersion(ACC_NO) } returns 1
            every { serializationService.serialize(newVersion, Properties(false)) } returns serializedSub
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } returnsArgument 0

            val result = testInstance.saveSubmissionRequest(request)

            assertThat(result).isEqualTo(ACC_NO to 2)
            val submissionRequest =
                DocSubmissionRequest(
                    ObjectId(),
                    ACC_NO,
                    2,
                    COPY,
                    "draftKey",
                    REQUESTED,
                    parse(serializedSub),
                    emptyList()
                )
            assertThat(subRequestSlot.captured).isEqualToIgnoringGivenFields(submissionRequest, "id")
        }

        @Test
        fun `save request with current version deleted`() {
            val subRequestSlot = slot<DocSubmissionRequest>()
            val newVersion = defaultSubmission(version = 3, status = ExtProcessingStatus.REQUESTED)
            val request = SubmissionRequest(defaultSubmission(version = 1), COPY, "draftKey")

            every { subDataRepository.getCurrentVersion(ACC_NO) } returns -2
            every { serializationService.serialize(newVersion, Properties(false)) } returns serializedSub
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } returnsArgument 0

            val result = testInstance.saveSubmissionRequest(request)

            assertThat(result).isEqualTo(ACC_NO to 3)
            val submissionRequest = DocSubmissionRequest(
                ObjectId(),
                ACC_NO,
                3,
                COPY,
                "draftKey",
                REQUESTED,
                parse(serializedSub),
                emptyList()
            )
            assertThat(subRequestSlot.captured).isEqualToIgnoringGivenFields(submissionRequest, "id")
        }

        @Test
        fun `save request when not current version`() {
            val subRequestSlot = slot<DocSubmissionRequest>()
            val newVersion = defaultSubmission(version = 1, status = ExtProcessingStatus.REQUESTED)
            val request = SubmissionRequest(defaultSubmission(version = 1), COPY, "draftKey")

            every { subDataRepository.getCurrentVersion(ACC_NO) } returns null
            every { serializationService.serialize(newVersion, Properties(false)) } returns serializedSub
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } returnsArgument 0

            val result = testInstance.saveSubmissionRequest(request)

            assertThat(result).isEqualTo(ACC_NO to 1)
            val submissionRequest = DocSubmissionRequest(
                ObjectId(),
                ACC_NO,
                1,
                COPY,
                "draftKey",
                REQUESTED,
                parse(serializedSub),
                emptyList()
            )
            assertThat(subRequestSlot.captured).isEqualToIgnoringGivenFields(submissionRequest, "id")
        }
    }

    @Test
    fun `process submission request`() {
        val submission = defaultSubmission()
        val request = SubmissionRequest(submission, COPY, "draftKey")
        val filePersistenceRequest = FilePersistenceRequest(request.submission, request.fileMode)
        every { systemService.persistSubmissionFiles(filePersistenceRequest) } returns submission
        every { submissionRepository.saveSubmission(submission, request.draftKey) } returns submission
        every { submissionRequestDocDataRepository.updateStatus(PROCESSED, submission.accNo, 1) } answers { nothing }

        val result = testInstance.processSubmissionRequest(request)

        assertThat(result).isEqualTo(submission)
    }
}
