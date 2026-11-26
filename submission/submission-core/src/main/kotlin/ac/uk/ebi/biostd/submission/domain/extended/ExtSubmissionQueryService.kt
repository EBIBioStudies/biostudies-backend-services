package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.request.SimpleFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionLinksPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.commons.OffsetBasedPageRequest
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import java.time.OffsetDateTime

class ExtSubmissionQueryService(
    private val filesRepository: SubmissionFilesPersistenceService,
    private val linksRepository: SubmissionLinksPersistenceService,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
) {
    suspend fun getExtendedSubmission(
        accNo: String,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission = submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles)

    suspend fun findExtendedSubmission(
        accNo: String,
        includeFileListFiles: Boolean = false,
    ): ExtSubmission? = submissionPersistenceQueryService.findExtByAccNo(accNo, includeFileListFiles)

    suspend fun getReferencedFiles(request: ExtFileListFilesRequest): Page<ExtFile> {
        val sub =
            submissionPersistenceQueryService.getExtByAccNo(
                accNo = request.accNo,
                includeFileListFiles = false,
                includeLinkListLinks = false,
            )
        var pageRequest = OffsetBasedPageRequest.fromOffsetAndLimit(request.page.offset, request.page.limit)
        return filesRepository.getReferencedFiles(
            sub,
            request.fileListPath,
            pageRequest,
        )
    }

    suspend fun getReferencedLinks(request: ExtLinkListFilesRequest): Page<ExtLink> {
        val sub =
            submissionPersistenceQueryService.getExtByAccNo(
                accNo = request.accNo,
                includeFileListFiles = false,
                includeLinkListLinks = false,
            )
        var pageRequest = OffsetBasedPageRequest.fromOffsetAndLimit(request.page.offset, request.page.limit)
        return linksRepository.getReferencedLinks(
            sub,
            request.linkListName,
            pageRequest,
        )
    }

    suspend fun getReferencedLinks(
        accNo: String,
        linkListName: String,
    ): ExtLinkTable {
        val links = linksRepository.getReferencedLinks(accNo, linkListName).toList()
        return ExtLinkTable(links.toList())
    }

    suspend fun getExtendedSubmissions(request: ExtSubPageRequest): Page<ExtSubmission> {
        val filter =
            SimpleFilter(
                rTimeFrom = request.fromRTime?.let { OffsetDateTime.parse(request.fromRTime) },
                rTimeTo = request.toRTime?.let { OffsetDateTime.parse(request.toRTime) },
                collection = request.collection,
                released = request.released,
                limit = request.limit,
                offset = request.offset,
            )
        val page = submissionPersistenceQueryService.getExtendedSubmissions(filter)
        return PageImpl(page.content, page.pageable, page.totalElements)
    }
}

class ExtSubPageRequest(
    val fromRTime: String? = null,
    val toRTime: String? = null,
    val released: Boolean? = null,
    val collection: String? = null,
    val offset: Long = 0,
    val limit: Int = 10,
)

data class ExtFileListFilesRequest(
    val accNo: String,
    val fileListPath: String,
    val page: ExtPageRequest,
)

data class ExtLinkListFilesRequest(
    val accNo: String,
    val linkListName: String,
    val page: ExtPageRequest,
)

data class ExtPageRequest(
    val offset: Long = 0L,
    val limit: Int = 10,
)
