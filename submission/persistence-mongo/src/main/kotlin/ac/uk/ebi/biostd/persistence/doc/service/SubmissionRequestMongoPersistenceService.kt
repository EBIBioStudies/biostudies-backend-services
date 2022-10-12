package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties

class SubmissionRequestMongoPersistenceService(
    private val serializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
) : SubmissionRequestPersistenceService {
    override fun hasActiveRequest(accNo: String): Boolean {
        return requestRepository.existsByAccNoAndStatusIn(accNo, RequestStatus.PROCESSING)
    }

    override fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override fun createSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.saveRequest(asRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override fun updateRequestStatus(accNo: String, version: Int, status: RequestStatus) {
        requestRepository.updateStatus(status, accNo, version)
    }

    override fun getPendingRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, REQUESTED)
    }

    override fun getLoadedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, LOADED)
    }

    override fun getCleanedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, CLEANED)
    }

    override fun getRequestStatus(accNo: String, version: Int): RequestStatus {
        return requestRepository.getByAccNoAndVersion(accNo, version).status
    }

    private fun asRequest(rqt: SubmissionRequest): DocSubmissionRequest {
        val content = serializationService.serialize(rqt.submission, Properties(includeFileListFiles = true))
        return DocSubmissionRequest(
            id = ObjectId(),
            accNo = rqt.submission.accNo,
            version = rqt.submission.version,
            draftKey = rqt.draftKey,
            status = rqt.status,
            submission = BasicDBObject.parse(content),
        )
    }

    private fun getRequest(accNo: String, version: Int, status: RequestStatus): SubmissionRequest {
        val request = requestRepository.getByAccNoAndVersionAndStatus(accNo, version, status)
        val stored = serializationService.deserialize(request.submission.toString())

        return SubmissionRequest(submission = stored, draftKey = request.draftKey, request.status)
    }
}
