package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.request.SimpleFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionLinksPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.doc.commons.OffsetBasedPageRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.toList
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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

    suspend fun getReferencedFiles(
        accNo: String,
        fileListName: String,
    ): ExtFileTable {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, false, false)
        val files = filesRepository.getReferencedFiles(sub, fileListName).toList()
        return ExtFileTable(files.toList())
    }

    suspend fun getReferencedFiles(request: ExtFileListFilesRequest): Page<ExtFile> {
        val sub = submissionPersistenceQueryService.getExtByAccNo(request.accNo, false, false)
        var pageRequest = OffsetBasedPageRequest.fromOffsetAndLimit(request.page.offset, request.page.limit)
        return filesRepository.getReferencedFiles(
            sub,
            request.fileListPath,
            pageRequest,
        )
    }

    suspend fun getReferencedLinks(request: ExtLinkListFilesRequest): Page<ExtLink> {
        val sub = submissionPersistenceQueryService.getExtByAccNo(request.accNo, false, false)
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
