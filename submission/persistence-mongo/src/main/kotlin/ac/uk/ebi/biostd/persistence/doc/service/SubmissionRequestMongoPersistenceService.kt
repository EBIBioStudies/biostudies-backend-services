package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFileChanges
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.ProcessResult
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFilesChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSED_STATUS
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSING_STATUS
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
    override suspend fun findAllProcessed(): Flow<Pair<String, Int>> =
        requestRepository
            .findByStatusIn(PROCESSED_STATUS)
            .map { it.accNo to it.version }

    override suspend fun hasActiveRequest(accNo: String): Boolean = requestRepository.existsByAccNoAndStatusIn(accNo, PROCESSING_STATUS)

    override fun getProcessingRequests(since: TemporalAmount?): Flow<Pair<String, Int>> {
        val request =
            when (since) {
                null -> requestRepository.findByStatusIn(PROCESSING_STATUS)
                else ->
                    requestRepository.findByStatusInAndModificationTimeLessThan(
                        PROCESSING_STATUS,
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

    override suspend fun updateRqtFile(rqt: SubmissionRequestFile) {
        requestRepository.updateSubRqtFile(rqt)
        requestRepository.increaseIndex(rqt.accNo, rqt.version)
    }

    override suspend fun getRequest(
        accNo: String,
        version: Int,
    ): SubmissionRequest {
        val docSubmissionRequest = requestRepository.getRequest(accNo, version)
        return asRequest(docSubmissionRequest)
    }

    override suspend fun onRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        processId: String,
        handler: suspend (SubmissionRequest) -> SubmissionRequest,
    ): SubmissionRequest {
        suspend fun loadRequest(): SubmissionRqt {
            val (changeId, request) = requestRepository.getRequest(accNo, version, status, processId)
            val stored = serializationService.deserialize(request.submission.toString())
            val subRequest = asRequest(request).copy(submission = stored)

            return changeId to subRequest
        }

        suspend fun onSuccess(
            rqt: SubmissionRequest,
            changeId: String,
        ) {
            logger.info { "Successfully completed request accNo='$accNo', version='$version', $status" }
            saveRequest(rqt, changeId, ProcessResult.SUCCESS)
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
        val fileChanges =
            DocFilesChanges(
                rqt.fileChanges.conflictingFiles,
                rqt.fileChanges.conflictingPageTab,
                rqt.fileChanges.deprecatedFiles,
                rqt.fileChanges.deprecatedPageTab,
                rqt.fileChanges.reusedFiles,
            )

        return DocSubmissionRequest(
            id = ObjectId(),
            accNo = rqt.submission.accNo,
            version = rqt.submission.version,
            draftKey = rqt.draftKey,
            notifyTo = rqt.notifyTo,
            status = rqt.status,
            submission = BasicDBObject.parse(content),
            totalFiles = rqt.totalFiles,
            fileChanges = fileChanges,
            currentIndex = rqt.currentIndex,
            previousVersion = rqt.previousVersion,
            silentMode = rqt.silentMode,
            processAll = rqt.processAll,
            modificationTime = rqt.modificationTime.toInstant(),
        )
    }

    private fun asRequest(rqt: DocSubmissionRequest): SubmissionRequest {
        val stored = serializationService.deserialize(rqt.submission.toString())
        val fileChanges =
            SubmissionRequestFileChanges(
                rqt.fileChanges.reusedFiles,
                rqt.fileChanges.deprecatedFiles,
                rqt.fileChanges.deprecatedPageTab,
                rqt.fileChanges.conflictingFiles,
                rqt.fileChanges.conflictingPageTab,
            )

        return SubmissionRequest(
            submission = stored,
            draftKey = rqt.draftKey,
            silentMode = rqt.silentMode,
            processAll = rqt.processAll,
            notifyTo = rqt.notifyTo,
            status = rqt.status,
            totalFiles = rqt.totalFiles,
            fileChanges = fileChanges,
            currentIndex = rqt.currentIndex,
            previousVersion = rqt.previousVersion,
            modificationTime = rqt.modificationTime.atOffset(UTC),
        )
    }
}
