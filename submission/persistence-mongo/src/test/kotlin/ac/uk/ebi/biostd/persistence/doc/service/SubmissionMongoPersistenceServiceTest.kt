package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED as REQUEST_PROCESSED
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission as submission

@ExtendWith(MockKExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK private val systemService: FileSystemService,
    @MockK private val dataRepository: SubmissionDocDataRepository,
    @MockK private val draftRepository: SubmissionDraftDocDataRepository,
    @MockK private val requestRepository: SubmissionRequestDocDataRepository,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val fileListDocFileRepository: FileListDocFileRepository
) {
    private val draftKey = "TMP_123456"

    private val docSubmission = slot<DocSubmission>()
    private val submissionSlot = slot<ExtSubmission>()
    private val submissionRequestSlot = slot<SubmissionRequest>()
    private val filePersistenceRequestSlot = slot<FilePersistenceRequest>()
    private val filesList = slot<List<FileListDocFile>>()
    private val statusSlot = slot<SubmissionRequestStatus>()
    private val accNoSlot = slot<String>()
    private val versionSlot = slot<Int>()
    private val filesListMock = mockk<List<FileListDocFile>>()
    private val docSubmissionMock = mockk<DocSubmission>()

    private val testInstance = SubmissionMongoPersistenceService(
        dataRepository,
        requestRepository,
        draftRepository,
        serializationService,
        systemService,
        fileListDocFileRepository
    )

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        setUpDataRepository()
        setUpDraftRepository()
        every { serializationService.serialize(capture(submissionSlot)) } returns "{}"
        every { systemService.persistSubmissionFiles(capture(filePersistenceRequestSlot)) } returns submission
        every { requestRepository.saveRequest(capture(submissionRequestSlot)) } answers { nothing }
        every { fileListDocFileRepository.saveAll(capture(filesList)) } answers { nothing }
        every {
            requestRepository.updateStatus(
                capture(statusSlot),
                capture(accNoSlot),
                capture(versionSlot)
            )
        } answers { nothing }
        mockkStatic("ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionKt")
        every { submission.copy(status = PROCESSING).toDocSubmission() } returns Pair(docSubmissionMock, filesListMock)
        every { docSubmissionMock.accNo } returns submission.accNo
        every { docSubmissionMock.owner } returns submission.owner
        every { docSubmissionMock.submitter } returns submission.submitter
        every { docSubmissionMock.version } returns submission.version
    }

    @Test
    fun `save and process submission request`() {
        testInstance.saveAndProcessSubmissionRequest(SaveSubmissionRequest(submission, MOVE, draftKey))

        assertSaveSubmissionRequest()
        assertUpdateSubmissionRequest()
        assertPersistedSubmission()
        verifyDraftRemovalByAccNo()
        verifyDraftRemovalByDraftKey()
        verifySubmissionProcessing()
    }

    @Test
    fun `save and process submission request without draft key`() {
        testInstance.saveAndProcessSubmissionRequest(SaveSubmissionRequest(submission, MOVE))

        assertSaveSubmissionRequest()
        assertUpdateSubmissionRequest()
        assertPersistedSubmission()
        verifyDraftRemovalByAccNo()
        verifySubmissionProcessing()
        verify(exactly = 0) {
            draftRepository.deleteByKey(draftKey)
        }
    }

    @Test
    fun `refresh submission`() {
        testInstance.refreshSubmission(submission)

        assertSaveSubmissionRequest()
        assertUpdateSubmissionRequest()
        assertPersistedSubmission()
        verifyDraftRemovalByAccNo()
        verifySubmissionProcessing()
    }

    private fun assertPersistedSubmission() {
        val persistedSubmission = submissionSlot.captured
        assertThat(persistedSubmission.version).isEqualTo(2)
        assertThat(persistedSubmission.status).isEqualTo(REQUESTED)

        val filePersistenceRequest = filePersistenceRequestSlot.captured
        assertThat(filePersistenceRequest.submission).isEqualTo(persistedSubmission)
        assertThat(filePersistenceRequest.mode).isEqualTo(MOVE)
        assertThat(filePersistenceRequest.previousFiles).isEmpty()
        verify(exactly = 1) { systemService.persistSubmissionFiles(filePersistenceRequest) }
    }

    private fun assertSaveSubmissionRequest() {
        val submissionRequest = submissionRequestSlot.captured
        assertThat(submissionRequest.accNo).isEqualTo(submission.accNo)
        assertThat(submissionRequest.version).isEqualTo(2)
        verify(exactly = 1) { requestRepository.saveRequest(submissionRequest) }
    }

    private fun assertUpdateSubmissionRequest() {
        val newStatus = statusSlot.captured
        val accNo = accNoSlot.captured
        val version = versionSlot.captured
        assertThat(newStatus).isEqualTo(REQUEST_PROCESSED)
        assertThat(accNo).isEqualTo(submissionSlot.captured.accNo)
        assertThat(version).isEqualTo(submissionSlot.captured.version)
        verify(exactly = 1) { requestRepository.updateStatus(newStatus, accNo, version) }
    }

    private fun verifyDraftRemovalByAccNo() = verify(exactly = 1) {
        draftRepository.deleteByUserIdAndKey(submission.owner, submission.accNo)
        draftRepository.deleteByUserIdAndKey(submission.submitter, submission.accNo)
    }

    private fun verifyDraftRemovalByDraftKey() = verify(exactly = 1) {
        draftRepository.deleteByKey(draftKey)
    }

    private fun verifySubmissionProcessing() = verify(exactly = 1) {
        dataRepository.expireActiveProcessedVersions(submission.accNo)
        dataRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
    }

    private fun setUpDataRepository() {
        every { dataRepository.getCurrentVersion(submission.accNo) } returns 1
        every { dataRepository.save(capture(docSubmission)) } answers { nothing }
        every { dataRepository.expireActiveProcessedVersions(submission.accNo) } answers { nothing }
        every { dataRepository.updateStatus(PROCESSED, submission.accNo, submission.version) } answers { nothing }
    }

    private fun setUpDraftRepository() {
        val owner = submission.owner
        val submitter = submission.submitter

        every { draftRepository.deleteByKey(draftKey) } answers { nothing }
        every { draftRepository.deleteByUserIdAndKey(owner, submission.accNo) } answers { nothing }
        every { draftRepository.deleteByUserIdAndKey(submitter, submission.accNo) } answers { nothing }
    }
}
