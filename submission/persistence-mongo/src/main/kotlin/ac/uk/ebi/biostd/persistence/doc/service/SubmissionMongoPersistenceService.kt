package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission

class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository
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
        return newVersion
    }

    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        TODO()
    }

    override fun refreshSubmission(submission: ExtSubmission) {
        TODO("Not yet implemented")
    }
}
