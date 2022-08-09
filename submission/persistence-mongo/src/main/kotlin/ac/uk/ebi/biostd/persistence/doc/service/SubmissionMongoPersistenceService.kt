package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED
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
        val version = getNextVersion(rqt.submission.accNo)
        val extSubmission = rqt.submission.copy(version = version)
        return saveRequest(rqt, extSubmission)
    }

    private fun saveRequest(rqt: SubmissionRequest, extSubmission: ExtSubmission): Pair<String, Int> {
        requestRepository.saveRequest(asRequest(rqt, extSubmission))
        return extSubmission.accNo to extSubmission.version
    }

    override fun saveSubmission(submission: ExtSubmission, draftKey: String?): ExtSubmission =
        submissionRepository.saveSubmission(submission, draftKey)

    override fun updateRequestAsProcessed(accNo: String, version: Int) {
        requestRepository.updateStatus(PROCESSED, accNo, version)
    }

    override fun setAsReleased(accNo: String) {
        subDataRepository.setAsReleased(accNo)
    }

    private fun asRequest(rqt: SubmissionRequest, submission: ExtSubmission): DocSubmissionRequest {
        val content = serializationService.serialize(submission, Properties(includeFileListFiles = true))
        return DocSubmissionRequest(
            id = ObjectId(),
            accNo = submission.accNo,
            version = submission.version,
            fileMode = rqt.fileMode,
            draftKey = rqt.draftKey,
            status = SubmissionRequestStatus.REQUESTED,
            submission = BasicDBObject.parse(content),
        )
    }
}
