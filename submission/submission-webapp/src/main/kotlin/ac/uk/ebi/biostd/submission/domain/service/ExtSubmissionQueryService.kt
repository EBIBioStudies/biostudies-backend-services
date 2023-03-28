package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

class ExtSubmissionQueryService(
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
) {

    fun getExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission =
        submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles)

    fun findExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission? =
        submissionPersistenceQueryService.findExtByAccNo(accNo, includeFileListFiles)

    fun getReferencedFiles(accNo: String, fileListName: String): ExtFileTable {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, false)
        val files = submissionPersistenceQueryService.getReferencedFiles(sub, fileListName)
        return ExtFileTable(files.toList())
    }

    fun getReferencedFile(accNo: String, fileListName: String): Sequence<ExtFile> {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, false)
        return submissionPersistenceQueryService.getReferencedFiles(sub, fileListName)
    }

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
