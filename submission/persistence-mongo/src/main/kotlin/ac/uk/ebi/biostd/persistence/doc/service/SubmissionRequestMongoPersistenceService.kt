package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CLEANED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.Companion.PROCESSING
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.FILES_COPIED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.LOADED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.REQUESTED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtFile
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.temporal.TemporalAmount

@Suppress("TooManyFunctions")
class SubmissionRequestMongoPersistenceService(
    private val serializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
) : SubmissionRequestPersistenceService {
    override fun hasActiveRequest(accNo: String): Boolean {
        return requestRepository.existsByAccNoAndStatusIn(accNo, PROCESSING)
    }

    override fun getProcessingRequests(since: TemporalAmount?): List<Pair<String, Int>> {
        val request = when (since) {
            null -> requestRepository.findByStatusIn(PROCESSING)
            else -> requestRepository.findByStatusInAndModificationTimeLessThan(PROCESSING, Instant.now().minus(since))
        }
        return request.map { it.accNo to it.version }
    }

    override fun saveSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override fun createSubmissionRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val (request, created) = requestRepository.saveRequest(asRequest(rqt))
        if (created.not()) throw ConcurrentSubException(request.accNo, request.version)
        return request.accNo to request.version
    }

    override fun updateRqtIndex(accNo: String, version: Int, index: Int) {
        requestRepository.updateIndex(accNo, version, index)
    }

    override fun updateRqtIndex(requestFile: SubmissionRequestFile, file: ExtFile) {
        requestRepository.updateIndex(requestFile.accNo, requestFile.version, requestFile.index)
        requestRepository.updateSubmissionRequestFile(requestFile.accNo, requestFile.version, requestFile.path, file)
    }

    override fun getPendingRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, REQUESTED)
    }

    override fun getIndexedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, INDEXED)
    }

    override fun getLoadedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, LOADED)
    }

    override fun getCleanedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, CLEANED)
    }

    override fun getCheckReleased(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, CHECK_RELEASED)
    }

    override fun getFilesCopiedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, FILES_COPIED)
    }

    override fun getPersistedRequest(accNo: String, version: Int): SubmissionRequest {
        return getRequest(accNo, version, PERSISTED)
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
            notifyTo = rqt.notifyTo,
            status = rqt.status,
            submission = BasicDBObject.parse(content),
            totalFiles = rqt.totalFiles,
            currentIndex = rqt.currentIndex,
            modificationTime = rqt.modificationTime.toInstant(),
        )
    }

    private fun getRequest(accNo: String, version: Int, status: RequestStatus): SubmissionRequest {
        val request = requestRepository.getByAccNoAndVersionAndStatus(accNo, version, status)
        val stored = serializationService.deserialize(request.submission.toString())
        return SubmissionRequest(
            submission = stored,
            draftKey = request.draftKey,
            request.notifyTo,
            request.status,
            request.totalFiles,
            request.currentIndex,
            request.modificationTime.atOffset(UTC),
        )
    }
}
