package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.RequestFileList
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject.parse
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.FileMode.COPY
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import uk.ac.ebi.extended.test.FileListFactory.defaultFileList
import uk.ac.ebi.extended.test.NfsFileFactory.defaultNfsFile
import uk.ac.ebi.extended.test.SectionFactory.defaultSection
import uk.ac.ebi.extended.test.SubmissionFactory.ACC_NO
import uk.ac.ebi.extended.test.SubmissionFactory.defaultSubmission
import java.io.OutputStream
import kotlin.io.path.ExperimentalPathApi

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK val subDataRepository: SubmissionDocDataRepository,
    @MockK val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    @MockK val serializationService: ExtSerializationService,
    @MockK val systemService: FileSystemService,
    @MockK val submissionRepository: ExtSubmissionRepository,
    val temporaryFolder: TemporaryFolder
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
        const val fileListSerialized = "{file-list}"
    }

    @Nested
    inner class SaveRequest {
        @Test
        fun `save request with current version active`() {
            val subRequestSlot = slot<DocSubmissionRequest>()

            val fileList = defaultFileList(files = listOf(defaultNfsFile()))
            val newVersion = defaultSubmission(version = 1, section = defaultSection(fileList = fileList))
            val expectedNewVersion = newVersion.copy(version = 2, status = ExtProcessingStatus.REQUESTED)
            val outputStream = slot<OutputStream>()
            val sequence = slot<Sequence<ExtFile>>()

            every { subDataRepository.getCurrentVersion(ACC_NO) } returns 1
            every { serializationService.serialize(expectedNewVersion, Properties(false)) } returns serializedSub
            every { serializationService.serialize(capture(sequence), capture(outputStream)) } answers { nothing }
            every { submissionRequestDocDataRepository.saveRequest(capture(subRequestSlot)) } returnsArgument 0

            val request = SubmissionRequest(newVersion.copy(version = 1), COPY, "draftKey")
            val result = testInstance.saveSubmissionRequest(request)
            assertThat(result).isEqualTo(ACC_NO to 2)

            val expectedFile = temporaryFolder.root.resolve(ACC_NO).resolve("2").resolve(fileList.fileName)
            assertThat(expectedFile.readText()).isEqualTo(EMPTY)

            val saved = subRequestSlot.captured
            assertThat(saved.accNo).isEqualTo(ACC_NO)
            assertThat(saved.version).isEqualTo(2)
            assertThat(saved.fileMode).isEqualTo(COPY)
            assertThat(saved.draftKey).isEqualTo("draftKey")
            assertThat(saved.status).isEqualTo(REQUESTED)
            assertThat(saved.submission).isEqualTo(parse(serializedSub))
            assertThat(saved.fileList).containsExactly(RequestFileList(fileList.fileName, expectedFile.absolutePath))
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

        val result = testInstance.processSubmissionRequest(request)

        assertThat(result).isEqualTo(sub)
        verify(exactly = 1) {
            subDataRepository.release(sub.accNo)
            submissionRepository.saveSubmission(sub, request.draftKey)
            systemService.persistSubmissionFiles(filePersistenceRequest)
            systemService.persistSubmissionFiles(filePersistenceRequest)
            systemService.releaseSubmissionFiles(sub.accNo, sub.owner, sub.relPath)
            submissionRequestDocDataRepository.updateStatus(PROCESSED, sub.accNo, 1)
        }
    }
}
