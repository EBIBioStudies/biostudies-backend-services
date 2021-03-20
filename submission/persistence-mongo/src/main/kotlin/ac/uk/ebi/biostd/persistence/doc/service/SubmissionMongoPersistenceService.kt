package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.math.absoluteValue

internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val systemService: FileSystemService,
    private val fileListDocFileRepository: FileListDocFileRepository
) : SubmissionRequestService {
    override fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val extended = saveSubmissionRequest(saveRequest)
        return processSubmission(SaveSubmissionRequest(extended, saveRequest.fileMode, saveRequest.draftKey))
    }

    override fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val submission = saveRequest.submission
        val newVersion = submission.copy(version = getNextVersion(submission.accNo), status = REQUESTED)
        submissionRequestDocDataRepository.saveRequest(asRequest(newVersion))
        return newVersion
    }

    private fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }

    private fun asRequest(submission: ExtSubmission) = SubmissionRequest(
        accNo = submission.accNo,
        version = submission.version,
        submission = BasicDBObject.parse(serializationService.serialize(submission))
    )

    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (submission, fileMode, draftKey) = saveRequest
        val processingSubmission = systemService.persistSubmissionFiles(submission, fileMode)
        val (docSubmission, files) = processingSubmission.copy(status = PROCESSING).toDocSubmission()
        saveSubmission(docSubmission, files, draftKey)

        return submission
    }

    private fun saveSubmission(docSubmission: DocSubmission, files: List<FileListDocFile>, draftKey: String?) {
        subDataRepository.save(docSubmission)
        fileListDocFileRepository.saveAll(files)
        updateCurrentRecords(docSubmission, draftKey)
        subDataRepository.updateStatus(PROCESSED, docSubmission.accNo, docSubmission.version)
    }

    private fun updateCurrentRecords(submission: DocSubmission, draftKey: String?) {
        subDataRepository.expireActiveProcessedVersions(submission.accNo)
        deleteSubmissionDrafts(submission, draftKey)
    }

    override fun refreshSubmission(submission: ExtSubmission) {
        saveAndProcessSubmissionRequest(SaveSubmissionRequest(submission, MOVE))
    }

    private fun deleteSubmissionDrafts(submission: DocSubmission, draftKey: String?) {
        draftKey?.let { draftDocDataRepository.deleteByKey(draftKey) }
        draftDocDataRepository.deleteByUserIdAndKey(submission.owner, submission.accNo)
        draftDocDataRepository.deleteByUserIdAndKey(submission.submitter, submission.accNo)
    }
}
