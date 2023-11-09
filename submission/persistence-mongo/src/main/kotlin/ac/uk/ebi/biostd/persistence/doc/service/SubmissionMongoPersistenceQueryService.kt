package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.Companion.FILE_PROCESSING_STAGES
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.Companion.WEIGHT_CONSTANT
import ac.uk.ebi.biostd.persistence.common.model.completion
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionRequestDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.getByAccNo
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.math.RoundingMode.HALF_UP
import kotlin.math.max

internal class SubmissionMongoPersistenceQueryService(
    private val submissionRepo: SubmissionDocDataRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper,
    private val serializationService: ExtSerializationService,
    private val requestRepository: SubmissionRequestDocDataRepository,
) : SubmissionPersistenceQueryService {
    override suspend fun existByAccNo(accNo: String): Boolean {
        return submissionRepo.existsByAccNo(accNo)
    }

    override suspend fun existByAccNoAndVersion(accNo: String, version: Int): Boolean {
        return submissionRepo.existsByAccNoAndVersion(accNo, version)
    }

    override suspend fun findExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission? {
        val findByAccNo = submissionRepo.findByAccNo(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override suspend fun findLatestInactiveByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission? {
        val findByAccNo = submissionRepo.findFirstByAccNoAndVersionLessThanOrderByVersion(accNo)
        return findByAccNo?.let { toExtSubmissionMapper.toExtSubmission(it, includeFileListFiles) }
    }

    override suspend fun getExtByAccNo(accNo: String, includeFileListFiles: Boolean): ExtSubmission {
        val submission = submissionRepo.getByAccNo(accNo)
        return toExtSubmissionMapper.toExtSubmission(submission, includeFileListFiles)
    }

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

    override suspend fun getSubmissionsByUser(filter: SubmissionListFilter): List<BasicSubmission> {
        val (requestsCount, requests) = requestRepository.findActiveRequests(filter)
        val submissionFilter = filter.copy(
            limit = filter.limit - requests.size,
            offset = max(0, filter.offset - requestsCount),
            notIncludeAccNo = requests.map { it.accNo }.toSet()
        )

        return requests
            .map { serializationService.deserialize(it.submission.toString()) to it.completionPercentage() }
            .map { it.first.asBasicSubmission(PROCESSING, it.second) }
            .plus(findSubmissions(submissionFilter))
    }

    private fun DocSubmissionRequest.completionPercentage(): Double {
        val fileCompletion = (currentIndex.toDouble() / totalFiles) * WEIGHT_CONSTANT
        val percentage = if (FILE_PROCESSING_STAGES.contains(status)) status.completion + fileCompletion
        else status.completion

        return percentage.toBigDecimal().setScale(2, HALF_UP).toDouble()
    }

    private suspend fun findSubmissions(filter: SubmissionListFilter): List<BasicSubmission> =
        when (filter.limit) {
            0 -> emptyList()
            else -> submissionRepo.getSubmissions(filter).map { it.asBasicSubmission(PROCESSED, 1.0) }.toList()
        }
}
