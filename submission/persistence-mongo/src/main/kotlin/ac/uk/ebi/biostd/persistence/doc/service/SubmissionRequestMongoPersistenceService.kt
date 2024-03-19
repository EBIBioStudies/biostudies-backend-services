package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.Companion.PROCESSING
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.OptResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
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

    private suspend fun saveRequest(rqt: SubmissionRequest): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asDocRequest(rqt))
        return rqt.submission.accNo to rqt.submission.version
    }

    override suspend fun createRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val (request, created) = requestRepository.saveRequest(asDocRequest(rqt))
        if (created.not()) throw ConcurrentSubException(request.accNo, request.version)
        return request.accNo to request.version
    }

    override suspend fun getRequestStatus(accNo: String, version: Int): RequestStatus {
        return requestRepository.getByAccNoAndVersion(accNo, version).status
    }

    override suspend fun updateRqtIndex(accNo: String, version: Int, index: Int) {
        requestRepository.updateIndex(accNo, version, index)
    }

    override suspend fun updateRqtIndex(requestFile: SubmissionRequestFile, file: ExtFile) {
        requestRepository.updateIndex(requestFile.accNo, requestFile.version, requestFile.index)
        requestRepository.updateSubmissionRequestFile(requestFile.accNo, requestFile.version, requestFile.path, file)
    }

    override suspend fun getSubmissionRequest(accNo: String, version: Int): SubmissionRequest {
        val docSubmissionRequest = requestRepository.getRequest(accNo, version)
        return asRequest(docSubmissionRequest)
    }

    override suspend fun <T> onRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        processId: String,
        handler: suspend (SubmissionRequest) -> OptResponse<T>,
    ): OptResponse<T> {
        suspend fun getSubmissionRequest(): SubmissionRqt {
            val (changeId, request) = requestRepository.getRequest(accNo, version, status, processId)
            val stored = serializationService.deserialize(request.submission.toString())
            val subRequest = SubmissionRequest(
                submission = stored,
                draftKey = request.draftKey,
                request.notifyTo,
                request.status,
                request.totalFiles,
                request.currentIndex,
                request.modificationTime.atOffset(UTC),
            )
            return changeId to subRequest
        }

        val (changeId, request) = getSubmissionRequest()
        val response = handler(request)
        saveRequest(response.rqt, changeId)
        return response
    }

    private suspend fun saveRequest(rqt: SubmissionRequest, changeId: String): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asDocRequest(rqt), changeId, Instant.now())
        return rqt.submission.accNo to rqt.submission.version
    }

    private fun asDocRequest(rqt: SubmissionRequest): DocSubmissionRequest {
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
            statusChanges = emptyList()
        )
    }

    private fun asRequest(request: DocSubmissionRequest): SubmissionRequest {
        val stored = serializationService.deserialize(request.submission.toString())
        val subRequest = SubmissionRequest(
            submission = stored,
            draftKey = request.draftKey,
            request.notifyTo,
            request.status,
            request.totalFiles,
            request.currentIndex,
            request.modificationTime.atOffset(UTC),
        )
        return subRequest
    }
}
