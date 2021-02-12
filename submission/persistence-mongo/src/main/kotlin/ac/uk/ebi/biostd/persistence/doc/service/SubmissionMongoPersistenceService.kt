package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import kotlin.math.absoluteValue

internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val systemService: FileSystemService
) : SubmissionRequestService {
    override fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val extended = saveSubmissionRequest(saveRequest)
        return processSubmission(SaveSubmissionRequest(extended, saveRequest.fileMode))
    }

    override fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val submission = saveRequest.submission
        val newVersion = submission.copy(
            version = getNextVersion(submission.accNo),
            status = REQUESTED)
//        subDataRepository.save(newVersion.toDocSubmission())
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
        submission = submission.toDocSubmission())

    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (submission, fileMode, accNo) = saveRequest
//        subDataRepository.updateStatus(PROCESSING, accNo, submission.version)
        val processingSubmission = systemService.persistSubmissionFiles(submission, fileMode)
        subDataRepository.save(processingSubmission.copy(status = ExtProcessingStatus.PROCESSING).toDocSubmission())
        processDbSubmission(processingSubmission)
        return submission
    }

    private fun processDbSubmission(submission: ExtSubmission): ExtSubmission {
        subDataRepository.expireActiveProcessedVersions(submission.accNo)
        draftDocDataRepository.deleteByUserIdAndKey(submission.submitter, submission.accNo)
        draftDocDataRepository.deleteByUserIdAndKey(submission.owner, submission.accNo)
//        subDataRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
        return submission
    }

    override fun refreshSubmission(submission: ExtSubmission) {
        saveAndProcessSubmissionRequest(SaveSubmissionRequest(submission.copy(version = submission.version + 1), MOVE))
    }
}
