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
import ac.uk.ebi.biostd.persistence.common.model.RequestStatusChanges
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestStatusChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.temporal.TemporalAmount

typealias SubmissionRqt = Pair<String, SubmissionRequest>

@Suppress("TooManyFunctions")
class SubmissionRequestMongoPersistenceService(
    private val serializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
) : SubmissionRequestPersistenceService {
    override suspend fun hasActiveRequest(accNo: String): Boolean {
        return requestRepository.existsByAccNoAndStatusIn(accNo, PROCESSING)
    }

    override fun getProcessingRequests(since: TemporalAmount?): Flow<Pair<String, Int>> {
        val request = when (since) {
            null -> requestRepository.findByStatusIn(PROCESSING)
            else -> requestRepository.findByStatusInAndModificationTimeLessThan(PROCESSING, Instant.now().minus(since))
        }
        return request.map { it.accNo to it.version }
    }

    override suspend fun saveRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override suspend fun createRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val (request, created) = requestRepository.saveRequest(asRequest(rqt))
        if (created.not()) throw ConcurrentSubException(request.accNo, request.version)
        return request.accNo to request.version
    }

    override suspend fun updateRqtIndex(accNo: String, version: Int, index: Int) {
        requestRepository.updateIndex(accNo, version, index)
    }

    override suspend fun updateRqtIndex(requestFile: SubmissionRequestFile, file: ExtFile) {
        requestRepository.updateIndex(requestFile.accNo, requestFile.version, requestFile.index)
        requestRepository.updateSubmissionRequestFile(requestFile.accNo, requestFile.version, requestFile.path, file)
    }

    override suspend fun getPendingRequest(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, REQUESTED, handlerName)
    }

    override suspend fun getIndexedRequest(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, INDEXED, handlerName)
    }

    override suspend fun getLoadedRequest(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, LOADED, handlerName)
    }

    override suspend fun getCleanedRequest(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, CLEANED, handlerName)
    }

    override suspend fun getCheckReleased(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, CHECK_RELEASED, handlerName)
    }

    override suspend fun getFilesCopiedRequest(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, FILES_COPIED, handlerName)
    }

    override suspend fun getPersistedRequest(accNo: String, version: Int, handlerName: String): SubmissionRqt {
        return getRequest(accNo, version, PERSISTED, handlerName)
    }

    override suspend fun getRequestStatus(accNo: String, version: Int): RequestStatus {
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
            statusChanges = rqt.statusChanges.map { asDocSubRequestStatusChange(it) }
        )
    }

    private fun asSubRequestStatusChange(doc: DocRequestStatusChanges): RequestStatusChanges {
        return RequestStatusChanges(
            doc.status,
            doc.statusId.toString(),
            doc.processId,
            doc.startTime,
            doc.endTime
        )
    }

    private fun asDocSubRequestStatusChange(rqt: RequestStatusChanges): DocRequestStatusChanges {
        return DocRequestStatusChanges(
            rqt.status,
            ObjectId(rqt.changeId),
            rqt.processId,
            rqt.startTime,
            rqt.endTime
        )
    }

    private suspend fun getRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        handlerName: String,
    ): Pair<String, SubmissionRequest> {
        val (statusId, request) = requestRepository.loadRequest(accNo, version, status, handlerName)
        val stored = serializationService.deserialize(request.submission.toString())
        val subRequest = SubmissionRequest(
            submission = stored,
            draftKey = request.draftKey,
            request.notifyTo,
            request.status,
            request.totalFiles,
            request.currentIndex,
            request.modificationTime.atOffset(UTC),
            request.statusChanges.map { asSubRequestStatusChange(it) }
        )
        return statusId to subRequest
    }
}
