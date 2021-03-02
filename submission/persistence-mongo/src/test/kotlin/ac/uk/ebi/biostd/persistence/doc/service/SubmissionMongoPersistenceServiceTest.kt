package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.test.fullExtSubmission
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK private val systemService: FileSystemService,
    @MockK private val dataRepository: SubmissionDocDataRepository,
    @MockK private val draftRepository: SubmissionDraftDocDataRepository,
    @MockK private val submissionRequestRepository: SubmissionRequestDocDataRepository,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val fileListDocFileRepository: FileListDocFileRepository
) {
    private val submission = fullExtSubmission
    private val docSubmission = slot<DocSubmission>()
    private val submissionSlot = slot<ExtSubmission>()
    private val submissionRequestSlot = slot<SubmissionRequest>()
    private val filesList = slot<List<FileListDocFile>>()

    private val testInstance = SubmissionMongoPersistenceService(
        dataRepository,
        submissionRequestRepository,
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
        every { systemService.persistSubmissionFiles(capture(submissionSlot), MOVE) } returns submission
        every { submissionRequestRepository.saveRequest(capture(submissionRequestSlot)) } answers { nothing }
        every { fileListDocFileRepository.saveAll(capture(filesList)) } answers { nothing }
    }

    @Test
    fun `save and process submission request`() {
        testInstance.saveAndProcessSubmissionRequest(SaveSubmissionRequest(submission, MOVE))

        assertSubmissionRequest()
        assertPersistedSubmission()
        verifySubmissionProcessing()
    }

    @Test
    fun `refresh submission`() {
        testInstance.refreshSubmission(submission)

        assertSubmissionRequest()
        assertPersistedSubmission()
        verifySubmissionProcessing()
    }

    private fun assertPersistedSubmission() {
        val persistedSubmission = submissionSlot.captured
        assertThat(persistedSubmission.version).isEqualTo(2)
        assertThat(persistedSubmission.status).isEqualTo(REQUESTED)
        verify(exactly = 1) { systemService.persistSubmissionFiles(persistedSubmission, MOVE) }
    }

    private fun assertSubmissionRequest() {
        val submissionRequest = submissionRequestSlot.captured
        assertThat(submissionRequest.accNo).isEqualTo(submission.accNo)
        assertThat(submissionRequest.version).isEqualTo(2)
        verify(exactly = 1) { submissionRequestRepository.saveRequest(submissionRequest) }
    }

    private fun verifySubmissionProcessing() = verify(exactly = 1) {
        dataRepository.expireActiveProcessedVersions(submission.accNo)
        draftRepository.deleteByUserIdAndKey(submission.owner, submission.accNo)
        dataRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
        draftRepository.deleteByUserIdAndKey(submission.submitter, submission.accNo)
    }

    private fun setUpDataRepository() {
        every { dataRepository.getCurrentVersion(submission.accNo) } returns 1
        every { dataRepository.save(capture(docSubmission)) } answers { nothing }
        every { dataRepository.expireActiveProcessedVersions(submission.accNo) } answers { nothing }
        every { dataRepository.updateStatus(PROCESSED, submission.accNo, submission.version) } answers { nothing }
    }

    private fun setUpDraftRepository() {
        every { draftRepository.deleteByUserIdAndKey(submission.owner, submission.accNo) } answers { nothing }
        every { draftRepository.deleteByUserIdAndKey(submission.submitter, submission.accNo) } answers { nothing }
    }
}
