package ac.uk.ebi.biostd.persistence.doc.service

import DefaultSubmission.Companion.ACC_NO
import DefaultSubmission.Companion.defaultSubmission
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties

@ExtendWith(MockKExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK val subDataRepository: SubmissionDocDataRepository,
    @MockK val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    @MockK val draftDocDataRepository: SubmissionDraftDocDataRepository,
    @MockK val serializationService: ExtSerializationService,
    @MockK val systemService: FileSystemService,
    @MockK val fileListDocFileRepository: FileListDocFileRepository,
    @MockK val toExtSubmissionMapper: ToExtSubmissionMapper
) {
    private val testInstance = SubmissionMongoPersistenceService(
        subDataRepository,
        submissionRequestDocDataRepository,
        draftDocDataRepository,
        serializationService,
        systemService,
        fileListDocFileRepository,
        toExtSubmissionMapper
    )

    @Test
    fun saveSubmissionRequest() {
        val newVersion = defaultSubmission(version = 2, status = REQUESTED)
        val request = SubmissionRequest(null, ACC_NO, 2, SubmissionRequestStatus.REQUESTED, BasicDBObject.parse("{}"))
        every { subDataRepository.getCurrentVersion(ACC_NO) } returns 1
        every { serializationService.serialize(newVersion, Properties(true)) } returns "{}"
        every { submissionRequestDocDataRepository.saveRequest(request) } answers { nothing }

        val result = testInstance.saveSubmissionRequest(defaultSubmission())

        assertThat(result).isEqualTo(newVersion)
    }

    @Test
    fun `processSubmission with null draftKey`() {
        val saveSubmissionRequest = SaveSubmissionRequest(defaultSubmission(), COPY, null)
        val finalExtSubmission: ExtSubmission = mockk()
        setupProcessSubmission(finalExtSubmission)

        val result = testInstance.processSubmission(saveSubmissionRequest)

        assertThat(result).isEqualTo(finalExtSubmission)
        verify(exactly = 0) { draftDocDataRepository.deleteByKey(any()) }
    }

    @Test
    fun `processSubmission with draftKey`() {
        val saveSubmissionRequest = SaveSubmissionRequest(defaultSubmission(), COPY, "draftKey")
        val finalExtSubmission: ExtSubmission = mockk()
        setupProcessSubmission(finalExtSubmission)

        val result = testInstance.processSubmission(saveSubmissionRequest)

        assertThat(result).isEqualTo(finalExtSubmission)
        verify(exactly = 1) { draftDocDataRepository.deleteByKey("draftKey") }
    }

    private fun setupProcessSubmission(
        finalExtSubmission: ExtSubmission
    ) {
        mockkStatic("ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionKt")

        val extSub: ExtSubmission = mockk()
        val docSub: DocSubmission = mockk()
        val files: List<FileListDocFile> = mockk()
        every { systemService.persistSubmissionFiles(FilePersistenceRequest(defaultSubmission(), COPY)) } returns extSub
        every { extSub.copy(status = PROCESSING).toDocSubmission() } returns Pair(docSub, files)
        every { subDataRepository.save(docSub) } answers { nothing }
        every { fileListDocFileRepository.saveAll(files) } answers { nothing }
        every { docSub.accNo } returns "doc-accNo"
        every { docSub.owner } returns "doc-owner"
        every { docSub.submitter } returns "doc-submitter"
        every { docSub.version } returns 1
        every { subDataRepository.expireActiveProcessedVersions("doc-accNo") } answers { nothing }
        every { subDataRepository.updateStatus(PROCESSED, "doc-accNo", 1) } answers { nothing }
        every { draftDocDataRepository.deleteByKey("draftKey") } answers { nothing }
        every { draftDocDataRepository.deleteByUserIdAndKey("doc-owner", "doc-accNo") } answers { nothing }
        every { toExtSubmissionMapper.toExtSubmission(docSub) } returns finalExtSubmission
        every { draftDocDataRepository.deleteByUserIdAndKey("doc-submitter", "doc-accNo") } answers { nothing }
    }
}
