package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequestStatus
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtProcessingStatus.REQUESTED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import kotlin.math.absoluteValue

@Suppress("LongParameterList")
internal class SubmissionMongoPersistenceService(
    private val subDataRepository: SubmissionDocDataRepository,
    private val submissionRequestDocDataRepository: SubmissionRequestDocDataRepository,
    private val serializationService: ExtSerializationService,
    private val systemService: FileSystemService,
    private val submissionRepository: ExtSubmissionRepository
) : SubmissionRequestService {

    override fun saveSubmissionRequest(submission: ExtSubmission): ExtSubmission {
        val newVersion = submission.copy(version = getNextVersion(submission.accNo), status = REQUESTED)
        submissionRequestDocDataRepository.saveRequest(asRequest(newVersion))
        return newVersion
    }

    override fun processSubmission(saveRequest: SaveSubmissionRequest): ExtSubmission {
        val (submission, fileMode, draftKey) = saveRequest
        val processingSubmission = processFiles(submission, fileMode)
        return submissionRepository.saveSubmission(processingSubmission, draftKey)
    }

    /**
     * Process the submission files. TODO: We need to populate previous files to avoid re creating them when using FIRE.
     */
    private fun processFiles(submission: ExtSubmission, fileMode: FileMode): ExtSubmission {
        val filePersistenceRequest = FilePersistenceRequest(submission, fileMode, emptyMap())
        return systemService.persistSubmissionFiles(filePersistenceRequest)
    }

    private fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }

    private fun asRequest(submission: ExtSubmission): SubmissionRequest {
        val content = serializationService.serialize(submission, Properties(includeFileListFiles = true))
        return SubmissionRequest(
            accNo = submission.accNo,
            version = submission.version,
            status = SubmissionRequestStatus.REQUESTED,
            submission = BasicDBObject.parse(content)
        )
    }
}
