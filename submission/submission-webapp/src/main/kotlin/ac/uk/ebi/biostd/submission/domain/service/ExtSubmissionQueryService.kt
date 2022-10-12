package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

class ExtSubmissionQueryService(
    private val requestService: SubmissionRequestPersistenceService,
    private val submissionFilesService: SubmissionFilesPersistenceService,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
) {
    fun hasActiveRequest(accNo: String): Boolean = requestService.hasActiveRequest(accNo)

    fun getExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission =
        submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles)

    fun findExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission? =
        submissionPersistenceQueryService.findExtByAccNo(accNo, includeFileListFiles)

    fun getReferencedFiles(accNo: String, fileListName: String): ExtFileTable =
        ExtFileTable(submissionFilesService.getReferencedFiles(accNo, fileListName))

    fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> {
        val filter = SubmissionFilter(
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
