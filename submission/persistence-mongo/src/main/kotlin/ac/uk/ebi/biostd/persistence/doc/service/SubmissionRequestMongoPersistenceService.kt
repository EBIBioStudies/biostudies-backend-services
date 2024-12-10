package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.SubmissionRequestDraftNotFoundException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFileChanges
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestProcessing
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestStatusChange
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.ProcessResult
import ac.uk.ebi.biostd.persistence.doc.db.data.ProcessResult.ERROR
import ac.uk.ebi.biostd.persistence.doc.db.data.ProcessResult.SUCCESS
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestFilesDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFilesChanges
import ac.uk.ebi.biostd.persistence.doc.model.DocRequestProcessing
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.mongodb.BasicDBObject
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.Companion.DRAFT_STATUS
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSED_STATUS
import ebi.ac.uk.model.RequestStatus.Companion.PROCESSING_STATUS
import ebi.ac.uk.model.RequestStatus.DRAFT
import ebi.ac.uk.model.RequestStatus.PROCESSED
import ebi.ac.uk.model.RequestStatus.SUBMITTED
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
    override suspend fun findRequestDrafts(
        owner: String,
        pageRequest: PageRequest,
    ): Flow<SubmissionRequest> {
        return requestRepository
            .findByOwnerAndStatusIn(owner, DRAFT_STATUS, pageRequest.asDataPageRequest())
            .map { asRequest(it) }
    }

    override suspend fun findRequestDraft(
        key: String,
        owner: String,
    ): SubmissionRequest? {
        return requestRepository.findByKeyAndOwnerAndStatusIn(key, owner, DRAFT_STATUS)?.let { asRequest(it) }
    }

    override suspend fun findSubmissionRequestDraft(accNo: String): SubmissionRequest? {
        return requestRepository.findByAccNoAndStatusIn(accNo, setOf(DRAFT))?.let { asRequest(it) }
    }

//    override suspend fun getActiveRequestDraft(
//        key: String,
//        owner: String,
//    ): SubmissionRequest {
//        val requestDraft =
//            requestRepository
//                .findByKeyAndOwnerAndStatusIn(key, owner, setOf(DRAFT))
//                ?: throw SubmissionRequestDraftNotFoundException(key, owner)
//
//        return asRequest(requestDraft)
//    }

    override suspend fun deleteRequestDraft(
        key: String,
        owner: String,
    ) {
        requestRepository.deleteByKeyAndOwnerAndStatusIn(key, owner, DRAFT_STATUS)
    }

    override suspend fun updateRequestDraft(
        key: String,
        owner: String,
        draft: String,
        modificationTime: Instant,
    ) {
        requestRepository.updateRqtDraft(key, owner, draft, modificationTime)
    }

    override suspend fun setDraftStatus(key: String, owner: String, status: RequestStatus, modificationTime: Instant) {
        requestRepository.setRequestDraftStatus(key, owner, status, modificationTime)
    }

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

        if (archivedFiles != countFiles) error("More files that archived identified in request $accNo, $version")
        requestRepository.deleteByAccNoAndVersion(accNo, version)
        requestFilesRepository.deleteByAccNoAndVersion(accNo, version)
    }

    override suspend fun createRequest(rqt: SubmissionRequest): Pair<String, Int> {
//        val (request, created) = requestRepository.saveRequest(asDocRequest(rqt))
        val request = requestRepository.saveRequest(asDocRequest(rqt))
//        if (created.not()) throw ConcurrentSubException(request.accNo, request.version)
        return request.accNo to request.version
    }

//    override suspend fun saveRequest(rqt: SubmissionRequest) {
//        requestRepository.updateRequest(asDocRequest(rqt))
//    }

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

    override suspend fun getSubmittedRequestDraft(key: String, owner: String): SubmissionRequest {
        val requestDraft =
            requestRepository
                .findByKeyAndOwnerAndStatusIn(key, owner, setOf(SUBMITTED))
                ?: throw SubmissionRequestDraftNotFoundException(key, owner)

        return asRequest(requestDraft)
    }

    override suspend fun onRequest(
        accNo: String,
        version: Int,
        status: RequestStatus,
        processId: String,
        handler: suspend (SubmissionRequest) -> SubmissionRequest,
    ): SubmissionRequest {
        suspend fun loadRequest(): SubmissionRqt {
            val (changeId, docRequest) = requestRepository.getRequest(accNo, version, status, processId)
            val stored = serializationService.deserialize(docRequest.process?.submission.toString())
            val subRequest = asRequest(docRequest, stored)

            return changeId to subRequest
        }

        suspend fun onSuccess(
            rqt: SubmissionRequest,
            changeId: String,
        ) {
            logger.info { "Successfully completed request accNo='$accNo', version='$version', $status" }
            saveRequest(rqt, changeId, SUCCESS)
        }

        suspend fun onError(
            it: Throwable,
            changeId: String,
            request: SubmissionRequest,
        ) {
            logger.error(it) {
                "Error on request accNo='$accNo', version='$version', changeId='$changeId', status='$status'"
            }
            saveRequest(request, changeId, ERROR)
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
        return rqt.accNo to rqt.version
    }

    private fun asDocRequest(rqt: SubmissionRequest): DocSubmissionRequest {
        fun requestProcessing(process: SubmissionRequestProcessing): DocRequestProcessing {
            val content = serializationService.serialize(process.submission, Properties(includeFileListFiles = true))
            val fileChanges =
                DocFilesChanges(
                    reusedFiles = process.fileChanges.reusedFiles,
                    deprecatedFiles = process.fileChanges.deprecatedFiles,
                    deprecatedPageTab = process.fileChanges.deprecatedPageTab,
                    conflictingFiles = process.fileChanges.conflictingFiles,
                    conflictingPageTab = process.fileChanges.conflictingPageTab,
                )

            return DocRequestProcessing(
                submission = BasicDBObject.parse(content),
                notifyTo = process.notifyTo,
                totalFiles = process.totalFiles,
                fileChanges = fileChanges,
                currentIndex = process.currentIndex,
                silentMode = process.silentMode,
                singleJobMode = process.singleJobMode,
                previousVersion = process.previousVersion,
            )
        }

        return DocSubmissionRequest(
            id = ObjectId(),
            key = rqt.key,
            accNo = rqt.accNo,
            version = rqt.version,
            owner = rqt.owner,
            draft = rqt.draft,
            status = rqt.status,
            process = rqt.process?.let { requestProcessing(it) },
            modificationTime = rqt.modificationTime.toInstant(),
        )
    }

    private fun asRequest(
        rqt: DocSubmissionRequest,
        sub: ExtSubmission? = null,
    ): SubmissionRequest {
        fun requestProcessing(process: DocRequestProcessing): SubmissionRequestProcessing {
            val stored = sub ?: serializationService.deserialize(process.submission.toString())
            val fileChanges =
                SubmissionRequestFileChanges(
                    reusedFiles = process.fileChanges.reusedFiles,
                    deprecatedFiles = process.fileChanges.deprecatedFiles,
                    deprecatedPageTab = process.fileChanges.deprecatedPageTab,
                    conflictingFiles = process.fileChanges.conflictingFiles,
                    conflictingPageTab = process.fileChanges.conflictingPageTab,
                )

            return SubmissionRequestProcessing(
                submission = stored,
                silentMode = process.silentMode,
                singleJobMode = process.singleJobMode,
                notifyTo = process.notifyTo,
                totalFiles = process.totalFiles,
                fileChanges = fileChanges,
                currentIndex = process.currentIndex,
                previousVersion = process.previousVersion,
                statusChanges = process.statusChanges.map { SubmissionRequestStatusChange(it.status) },
            )
        }

        return SubmissionRequest(
            key = rqt.key,
            accNo = rqt.accNo,
            version = rqt.version,
            owner = rqt.owner,
            draft = rqt.draft,
            process = rqt.process?.let { requestProcessing(it) },
            status = rqt.status,
            modificationTime = rqt.modificationTime.atOffset(UTC),
        )
    }
}
