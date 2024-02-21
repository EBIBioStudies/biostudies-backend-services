package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.request.SimpleFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

class ExtSubmissionQueryService(
    private val filesRepository: SubmissionFilesPersistenceService,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
) {
    suspend fun getExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission =
        submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles)

    suspend fun getExtSubmissionByAccNoAndVersion(
        accNo: String,
        version: Int,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission {
        return submissionPersistenceQueryService.getExtByAccNoAndVersion(accNo, version, includeFileListFiles)
    }

    suspend fun findExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission? =
        submissionPersistenceQueryService.findExtByAccNo(accNo, includeFileListFiles)

    suspend fun getReferencedFiles(accNo: String, fileListName: String): ExtFileTable {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, false)
        val files = filesRepository.getReferencedFiles(sub, fileListName).toList()
        return ExtFileTable(files.toList())
    }

    suspend fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> {
        val filter = SimpleFilter(
            rTimeFrom = request.fromRTime?.let { OffsetDateTime.parse(request.fromRTime) },
            rTimeTo = request.toRTime?.let { OffsetDateTime.parse(request.toRTime) },
            collection = request.collection,
            released = request.released,
            limit = request.limit,
            offset = request.offset
        )
        val page = submissionPersistenceQueryService.getExtendedSubmissions(filter)
        return PageImpl(page.content, page.pageable, page.totalElements)
    }
}

class ExtPageRequest(
    val fromRTime: String? = null,
    val toRTime: String? = null,
    val released: Boolean? = null,
    val collection: String? = null,
    val offset: Long,
    val limit: Int,
)
