package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus.PROCESSING
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import org.json.JSONObject
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    private val systemService: FileSystemService,
    private val serializationService: ExtSerializationService
) : SubmissionRequestService {

    override fun saveAndProcessSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val extended = saveSubmissionRequest(saveRequest)
        return processSubmission(SaveSubmissionRequest(extended, saveRequest.fileMode))
    }

    override fun saveSubmissionRequest(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val submission = saveRequest.submission
        val newVersion = submission.copy(
            version = subDataRepository.getCurrentVersion(submission.accNo) ?: 0,
            status = ExtProcessingStatus.REQUESTED)
        subDataRepository.save(newVersion.toDocSubmission())
        submissionRequestDocDataRepository.saveRequest(asRequest(newVersion))
        return newVersion
    }

    private fun asRequest(submission: ExtSubmission) = SubmissionRequest(
        accNo = submission.accNo,
        version = submission.version,
        request = JSONObject(serializationService.serialize(submission)))

    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (submission, fileMode, accNo) = saveRequest
        subDataRepository.updateStatus(PROCESSING, accNo, submission.version)
        systemService.persistSubmissionFiles(submission, fileMode)
        processDbSubmission(submission)
        return submission
    }

    private fun processDbSubmission(submission: ExtSubmission): ExtSubmission {
        subDataRepository.expireActiveProcessedVersions(submission.accNo)
        subDataRepository.deleteSubmissionDrafts(submission.submitter, submission.accNo)
        subDataRepository.deleteSubmissionDrafts(submission.owner, submission.accNo)
        subDataRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
        return submission
    }

    override fun refreshSubmission(submission: ExtSubmission) {
        TODO("Not yet implemented")
    }
}
