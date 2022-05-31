package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import mu.KotlinLogging
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import kotlin.math.absoluteValue

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val systemService: FileSystemService,
    private val submissionRepository: ExtSubmissionRepository,
) : SubmissionPersistenceService {

    override fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }

    override fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val extSubmission = rqt.submission.copy(status = REQUESTED)
        return saveRequest(rqt, extSubmission)
    }

    private fun saveRequest(rqt: SubmissionRequest, extSubmission: ExtSubmission): Pair<String, Int> {
        requestRepository.saveRequest(asRequest(rqt, extSubmission))
        return extSubmission.accNo to extSubmission.version
    }

    override fun processSubmissionRequest(saveRequest: SubmissionRequest): ExtSubmission {
        val (submission, fileMode, draftKey) = saveRequest
        val processingSubmission = processFiles(submission, fileMode)
        val savedSubmission = submissionRepository.saveSubmission(processingSubmission, draftKey)
        requestRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
        systemService.unpublishSubmissionFiles(savedSubmission.accNo, savedSubmission.owner, savedSubmission.relPath)

        if (savedSubmission.released) {
            releaseSubmission(savedSubmission.accNo, savedSubmission.owner, savedSubmission.relPath)
        }

        return savedSubmission
    }

    override fun releaseSubmission(accNo: String, owner: String, relPath: String) {
        logger.info { "$accNo $owner Releasing submission $accNo" }

        subDataRepository.release(accNo)
        systemService.releaseSubmissionFiles(accNo, owner, relPath)

        logger.info { "$accNo $owner Finished releasing submission $accNo" }
    }

    private fun processFiles(submission: ExtSubmission, fileMode: FileMode): ExtSubmission {
        val filePersistenceRequest = FilePersistenceRequest(submission, fileMode)
        return systemService.persistSubmissionFiles(filePersistenceRequest)
    }

    private fun asRequest(rqt: SubmissionRequest, submission: ExtSubmission): DocSubmissionRequest {
        val content = serializationService.serialize(submission, Properties(includeFileListFiles = true))
        return DocSubmissionRequest(
            id = getId(submission),
            accNo = submission.accNo,
            version = submission.version,
            fileMode = rqt.fileMode,
            draftKey = rqt.draftKey,
            status = SubmissionRequestStatus.REQUESTED,
            submission = BasicDBObject.parse(content),
        )
    }

    private fun getId(sub: ExtSubmission): ObjectId {
        val request = requestRepository.findByAccNoAndVersionAndStatus(
            sub.accNo,
            sub.version,
            SubmissionRequestStatus.REQUESTED
        )
        return request?.id ?: ObjectId()
    }
}
