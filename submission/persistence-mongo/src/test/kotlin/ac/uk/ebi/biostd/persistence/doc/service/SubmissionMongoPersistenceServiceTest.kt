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
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import io.mockk.mockk
import io.mockk.slot
import io.mockk.every
import io.mockk.clearAllMocks
import io.mockk.verify
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED as REQUEST_PROCCESSED

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class SubmissionMongoPersistenceServiceTest(
    @MockK private val systemService: FileSystemService,
    @MockK private val dataRepository: SubmissionDocDataRepository,
    @MockK private val draftRepository: SubmissionDraftDocDataRepository,
    @MockK private val submissionRequestRepository: SubmissionRequestDocDataRepository,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val fileListDocFileRepository: FileListDocFileRepository,
    private val tempFolder: TemporaryFolder
) {
    private val draftKey = "TMP_123456"

    private val section = mockk<ExtSection>()
    private val submission = fullExtSubmission.copy(section = section)
    private val docSubmission = slot<DocSubmission>()
    private val submissionSlot = slot<ExtSubmission>()
    private val submissionRequestSlot = slot<SubmissionRequest>()
    private val filesList = slot<List<FileListDocFile>>()
    private val requestStatusSlot = slot<SubmissionRequestStatus>()
    private val accNoSlot = slot<String>()
    private val versionSlot = slot<Int>()

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
        every {
            submissionRequestRepository.updateStatus(
                capture(requestStatusSlot), capture(accNoSlot), capture(versionSlot)
            )
        } answers { nothing }
        every { section.sections } answers { listOf() }
        every { section.fileList } answers { nothing }
        every { section.accNo } answers { nothing }
        every { section.type } answers { "someAnswer" }
        every { section.attributes } answers { listOf() }
        every { section.files } answers { listOf() }
        every { section.links } answers { listOf() }
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
        verify(exactly = 1) { systemService.persistSubmissionFiles(persistedSubmission, MOVE) }
    }

    private fun assertSaveSubmissionRequest() {
        val submissionRequest = submissionRequestSlot.captured
        assertThat(submissionRequest.accNo).isEqualTo(submission.accNo)
        assertThat(submissionRequest.version).isEqualTo(2)
        verify(exactly = 1) { submissionRequestRepository.saveRequest(submissionRequest) }
    }

    private fun assertUpdateSubmissionRequest() {
        val newStatus = requestStatusSlot.captured
        val accNo = accNoSlot.captured
        val version = versionSlot.captured
        assertThat(newStatus).isEqualTo(REQUEST_PROCCESSED)
        assertThat(accNo).isEqualTo(submissionSlot.captured.accNo)
        assertThat(version).isEqualTo(submissionSlot.captured.version)
        verify(exactly = 1) { submissionRequestRepository.updateStatus(newStatus, accNo, version) }
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
