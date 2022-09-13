package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtSubmission
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import kotlin.math.absoluteValue

@Suppress("LongParameterList")
internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val submissionRepository: ExtSubmissionRepository,
) : SubmissionPersistenceService {
    override fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }

    override fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override fun createSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.saveRequest(asRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override fun saveSubmission(submission: ExtSubmission): ExtSubmission =
        submissionRepository.saveSubmission(submission)

    override fun updateRequestStatus(accNo: String, version: Int, status: RequestStatus) {
        requestRepository.updateStatus(status, accNo, version)
    }

    override fun setAsReleased(accNo: String) {
        subDataRepository.setAsReleased(accNo)
    }

    override fun expirePreviousVersions(accNo: String) {
        submissionRepository.expirePreviousVersions(accNo)
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
}
