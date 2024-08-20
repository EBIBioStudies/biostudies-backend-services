package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.OptResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.ProcessResult
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSING
import ebi.ac.uk.model.RequestStatus.PROCESSED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import org.bson.types.ObjectId
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.temporal.TemporalAmount

typealias SubmissionRqt = Pair<String, SubmissionRequest>

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class SubmissionRequestMongoPersistenceService(
    private val serializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
    private val requestFilesRepository: SubmissionRequestFilesDocDataRepository,
    private val distributedLockService: DistributedLockService,
) : SubmissionRequestPersistenceService {
    override suspend fun hasActiveRequest(accNo: String): Boolean = requestRepository.existsByAccNoAndStatusIn(accNo, PROCESSING)

    override fun getProcessingRequests(since: TemporalAmount?): Flow<Pair<String, Int>> {
        val request =
            when (since) {
                null -> requestRepository.findByStatusIn(PROCESSING)
                else ->
                    requestRepository.findByStatusInAndModificationTimeLessThan(
                        PROCESSING,
                        Instant.now().minus(since),
                    )
            }
        return request.map { it.accNo to it.version }
    }

    override suspend fun archiveRequest(
        accNo: String,
        version: Int,
    ) {
        require(
            requestRepository.existsByAccNoAndVersionAndStatus(accNo, version, PROCESSED),
        ) { "Request $accNo, $version can not be archived as not processed" }

        val archivedFiles = requestRepository.archiveRequest(accNo, version)
        val countFiles = requestFilesRepository.countByAccNoAndVersion(accNo, version)

        if (archivedFiles != countFiles) error("More files that archived identitified in request $accNo, $version")
        requestRepository.deleteByAccNoAndVersion(accNo, version)
        requestFilesRepository.deleteByAccNoAndVersion(accNo, version)
    }

    override suspend fun createRequest(rqt: SubmissionRequest): Pair<String, Int> {
        val (request, created) = requestRepository.saveRequest(asDocRequest(rqt))
        if (created.not()) throw ConcurrentSubException(request.accNo, request.version)
        return request.accNo to request.version
    }

    override suspend fun getRequestStatus(
        accNo: String,
        version: Int,
    ): RequestStatus = requestRepository.getByAccNoAndVersion(accNo, version).status

    override suspend fun updateRqtFile(rqt: SubmissionRequestFile) {
        requestRepository.updateSubRqtFile(rqt)
        requestRepository.increaseIndex(rqt.accNo, rqt.version)
    }

    override suspend fun getSubmissionRequest(
        accNo: String,
        version: Int,
    ): SubmissionRequest {
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
        suspend fun loadRequest(): SubmissionRqt {
            val (changeId, request) = requestRepository.getRequest(accNo, version, status, processId)
            val stored = serializationService.deserialize(request.submission.toString())
            val subRequest =
                SubmissionRequest(
                    submission = stored,
                    draftKey = request.draftKey,
                    silentMode = request.silentMode,
                    notifyTo = request.notifyTo,
                    status = request.status,
                    conflictingFiles = request.conflictingFiles,
                    conflictingPageTab = request.conflictingPageTab,
                    deprecatedFiles = request.deprecatedFiles,
                    deprecatedPageTab = request.deprecatedPageTab,
                    reusedFiles = request.reusedFiles,
                    totalFiles = request.totalFiles,
                    currentIndex = request.currentIndex,
                    previousVersion = request.previousVersion,
                    modificationTime = request.modificationTime.atOffset(UTC),
                )
            return changeId to subRequest
        }

        suspend fun <T> onSuccess(
            it: OptResponse<T>,
            changeId: String,
        ) {
            logger.info { "Succefully completed request accNo='$accNo', version='$version', $status" }
            saveRequest(it.rqt, changeId, ProcessResult.SUCCESS)
        }

        suspend fun onError(
            it: Throwable,
            changeId: String,
            request: SubmissionRequest,
        ) {
            logger.error(it) {
                "Error on request accNo='$accNo', version='$version', changeId='$changeId', status='$status'"
            }
            saveRequest(request, changeId, ProcessResult.ERROR)
        }

        val (changeId, request) = loadRequest()
        return runCatching { distributedLockService.onLockRequest(accNo, version, processId) { handler(request) } }
            .onSuccess { onSuccess(it, changeId) }
            .onFailure { onError(it, changeId, request) }
            .getOrThrow()
    }

    override suspend fun isRequestCompleted(
        accNo: String,
        version: Int,
    ): Boolean = requestRepository.existsByAccNoAndVersionAndStatus(accNo, version, PROCESSED)

    private suspend fun saveRequest(
        rqt: SubmissionRequest,
        changeId: String,
        result: ProcessResult,
    ): Pair<String, Int> {
        requestRepository.updateSubmissionRequest(asDocRequest(rqt), changeId, Instant.now(), result)
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
            conflictingFiles = rqt.conflictingFiles,
            conflictingPageTab = rqt.conflictingPageTab,
            deprecatedFiles = rqt.deprecatedFiles,
            deprecatedPageTab = rqt.deprecatedPageTab,
            reusedFiles = rqt.reusedFiles,
            currentIndex = rqt.currentIndex,
            previousVersion = rqt.previousVersion,
            silentMode = rqt.silentMode,
            modificationTime = rqt.modificationTime.toInstant(),
        )
    }

    private fun asRequest(request: DocSubmissionRequest): SubmissionRequest {
        val stored = serializationService.deserialize(request.submission.toString())
        val subRequest =
            SubmissionRequest(
                submission = stored,
                draftKey = request.draftKey,
                silentMode = request.silentMode,
                notifyTo = request.notifyTo,
                status = request.status,
                totalFiles = request.totalFiles,
                conflictingFiles = request.conflictingFiles,
                conflictingPageTab = request.deprecatedPageTab,
                deprecatedFiles = request.deprecatedFiles,
                deprecatedPageTab = request.deprecatedPageTab,
                reusedFiles = request.reusedFiles,
                currentIndex = request.currentIndex,
                previousVersion = request.previousVersion,
                modificationTime = request.modificationTime.atOffset(UTC),
            )
        return subRequest
    }
}
