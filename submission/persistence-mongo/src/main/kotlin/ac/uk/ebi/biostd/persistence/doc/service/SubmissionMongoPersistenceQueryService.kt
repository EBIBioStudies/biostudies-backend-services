package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.doc.model.asSubmittedRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.RequestStatus
import ebi.ac.uk.model.RequestStatus.SUBMITTED
import ebi.ac.uk.model.constants.ProcessingStatus.INVALID
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.math.max

@Suppress("TooManyFunctions")
internal class SubmissionMongoPersistenceQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val extSerializationService: ExtSerializationService,
    private val serializationService: SerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
) : SubmissionPersistenceQueryService {
    override suspend fun existByAccNo(accNo: String): Boolean = submissionRepo.existsByAccNo(accNo)

    override suspend fun existByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): Boolean = submissionRepo.existsByAccNoAndVersion(accNo, version)

    override suspend fun existActiveByAccNo(accNo: String): Boolean = submissionRepo.existsByAccNoAndVersionGreaterThan(accNo, 0)

    override suspend fun findExtByAccNo(
        accNo: String,
        includeFileListFiles: Boolean,
    ): ExtSubmission? {
        val findByAccNo = submissionRepo.findByAccNo(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override suspend fun findLatestInactiveByAccNo(
        accNo: String,
        includeFileListFiles: Boolean,
    ): ExtSubmission? {
        val findByAccNo = submissionRepo.findFirstByAccNoAndVersionLessThanOrderByVersion(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override suspend fun getExtByAccNo(
        accNo: String,
        includeFileListFiles: Boolean,
    ): ExtSubmission {
        val submission = submissionRepo.getByAccNo(accNo)
        return toExtSubmissionMapper.toExtSubmission(submission, includeFileListFiles)
    }

    override suspend fun findAllActive(includeFileListFiles: Boolean): Flow<ExtSubmission> =
        submissionRepo
            .findAllByVersionGreaterThan(0)
            .map { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }

    override suspend fun getExtByAccNoAndVersion(
        accNo: String,
        version: Int,
        includeFileListFiles: Boolean,
    ): ExtSubmission {
        val document = submissionRepo.getByAccNoAndVersion(accNo, version)
        return toExtSubmissionMapper.toExtSubmission(document, includeFileListFiles)
    }

    override suspend fun getExtendedSubmissions(filter: SubmissionFilter): Page<ExtSubmission> {
        val page = submissionRepo.getSubmissionsPage(filter)
        val items = page.content.map { toExtSubmissionMapper.toExtSubmission(it, false) }
        return PageImpl(items.toList(), PageRequest.of(filter.pageNumber, filter.limit), page.totalElements)
    }

    override suspend fun findCoreInfo(accNo: String): ExtSubmissionInfo? {
        val sub = submissionRepo.findByAccNo(accNo)
        return sub?.let { Info(it.accNo, it.version, it.owner, it.released, it.secretKey, it.relPath, it.storageMode) }
    }

    override suspend fun getCoreInfoByAccNoAndVersion(
        accNo: String,
        version: Int,
    ): ExtSubmissionInfo {
        val sub = submissionRepo.getByAccNoAndVersion(accNo, version)
        return Info(sub.accNo, sub.version, sub.owner, sub.released, sub.secretKey, sub.relPath, sub.storageMode)
    }

    private data class Info(
        override val accNo: String,
        override val version: Int,
        override val owner: String,
        override val released: Boolean,
        override val secretKey: String,
        override val relPath: String,
        override val storageMode: StorageMode,
    ) : ExtSubmissionInfo

    override suspend fun getSubmissionsByUser(filter: SubmissionListFilter): List<BasicSubmission> {
        val (requestsCount, requests) = requestRepository.findActiveRequests(filter)
        val submissionFilter =
            filter.copy(
                limit = filter.limit - requests.size,
                offset = max(0, filter.offset - requestsCount),
                notIncludeAccNo = requests.map { it.accNo }.toSet(),
            )

        return requests
            .map { asBasicSubmission(it) }
            .plus(findSubmissions(submissionFilter))
    }

    private fun asBasicSubmission(rqt: DocSubmissionRequest): BasicSubmission {
        if (rqt.status == SUBMITTED) {
            val submission = serializationService.deserializeSubmission(rqt.draft.toString(), JSON)
            return submission.asSubmittedRequest(rqt.owner)
        }

        val submission = extSerializationService.deserialize(rqt.process?.submission.toString())
        return when (rqt.status) {
            RequestStatus.INVALID -> submission.asBasicSubmission(INVALID, rqt.errors)
            else -> submission.asBasicSubmission(PROCESSING)
        }
    }

    private suspend fun findSubmissions(filter: SubmissionListFilter): List<BasicSubmission> =
        when (filter.limit) {
            0 -> emptyList()
            else -> submissionRepo.getSubmissions(filter).map { it.asBasicSubmission(PROCESSED) }.toList()
        }
}
