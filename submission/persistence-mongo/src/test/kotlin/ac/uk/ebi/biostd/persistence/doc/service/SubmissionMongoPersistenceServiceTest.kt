package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject.parse
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
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
import ebi.ac.uk.test.basicExtSubmission as extSub

@ExtendWith(MockKExtension::class)
internal class SubmissionMongoPersistenceServiceTest(
    @MockK private val subDataRepository: SubmissionDocDataRepository,
    @MockK private val subRequestDocDataRepo: SubmissionRequestDocDataRepository,
    @MockK private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    @MockK private val serializationService: ExtSerializationService,
    @MockK private val systemService: FileSystemService,
    @MockK private val fileListDocFileRepository: FileListDocFileRepository,
    @MockK private val toExtSubmissionMapper: ToExtSubmissionMapper
) {

    private val testInstance = SubmissionMongoPersistenceService(
        subDataRepository,
        subRequestDocDataRepo,
        draftDocDataRepository,
        serializationService,
        systemService,
        fileListDocFileRepository,
        toExtSubmissionMapper
    )

    private val extSubNewVersion = extSub.copy(version = 1, status = ExtProcessingStatus.REQUESTED)
    private val processingSub = mockk<ExtSubmission>()
    val fileMode = mockk<FileMode>()
    private val docSub = mockk<DocSubmission>()
    private val extSubResult = mockk<ExtSubmission>()
    private val docFileList = mockk<FileListDocFile>()

    @Test
    fun saveSubmissionRequest() {
        every { subDataRepository.getCurrentVersion(extSub.accNo) } returns null
        every { serializationService.serialize(extSubNewVersion, any()) } returns "{}"
        every {
            subRequestDocDataRepo.saveRequest(SubmissionRequest(null, extSub.accNo, 1, REQUESTED, parse("{}")))
        } answers { nothing }

        val result = testInstance.saveSubmissionRequest(SaveSubmissionRequest(extSub, fileMode, null))

        assertThat(result).isEqualTo(extSubNewVersion)
    }

    @Test
    fun processSubmission() {
        mockkStatic("ac.uk.ebi.biostd.persistence.doc.mapping.from.ToDocSubmissionKt")

        every { systemService.persistSubmissionFiles(FilePersistenceRequest(extSub, fileMode)) } returns processingSub
        every { processingSub.copy(status = PROCESSING).toDocSubmission() } returns Pair(docSub, listOf(docFileList))
        every { subDataRepository.save(docSub) } answers { nothing }
        every { fileListDocFileRepository.saveAll(listOf(docFileList)) } answers { nothing }
        every { docSub.accNo } returns "accNo"
        every { docSub.owner } returns "docSubOwner"
        every { docSub.submitter } returns "docSubSubmitter"
        every { docSub.version } returns 1
        every { subDataRepository.expireActiveProcessedVersions("accNo") } answers { nothing }
        every { draftDocDataRepository.deleteByUserIdAndKey("docSubOwner", "accNo") } answers { nothing }
        every { draftDocDataRepository.deleteByUserIdAndKey("docSubSubmitter", "accNo") } answers { nothing }
        every { subDataRepository.updateStatus(DocProcessingStatus.PROCESSED, "accNo", 1) } answers { nothing }
        every { subRequestDocDataRepo.updateStatus(PROCESSED, extSub.accNo, extSub.version) } answers { nothing }
        every { toExtSubmissionMapper.toExtSubmission(docSub) } returns extSubResult

        val result = testInstance.processSubmission(SaveSubmissionRequest(extSub, fileMode, null))

        assertThat(result).isEqualTo(extSubResult)
        verify(exactly = 0) { draftDocDataRepository.deleteByKey(any()) }
    }
}
