package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.domain.extended.ExtFileListFilesRequest
import ac.uk.ebi.biostd.submission.domain.extended.ExtLinkListFilesRequest
import ac.uk.ebi.biostd.submission.domain.extended.ExtPageRequest
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubPageRequest
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.web.resources.ext.mapping.ExtendedFilePageMapper
import ac.uk.ebi.biostd.submission.web.resources.ext.mapping.ExtendedLinkPageMapper
import ac.uk.ebi.biostd.submission.web.resources.ext.mapping.ExtendedSubmissionPageMapper
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.WebExtPage
import ebi.ac.uk.model.FileListPath
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
@Tag(name = "Extended Submissions", description = "Query extended submission documents, file lists, and link lists.")
@Suppress("TooManyFunctions")
class ExtQuerySubmissionResource(
    private val extendedFilePageMapper: ExtendedFilePageMapper,
    private val extendedLinkPageMapper: ExtendedLinkPageMapper,
    private val extPageMapper: ExtendedSubmissionPageMapper,
    private val extSubmissionQueryService: ExtSubmissionQueryService,
) {
    @GetMapping("/{accNo}/referencedFiles/**")
    @Operation(
        summary = "Get Referenced Files",
        description = "Return files referenced by a path inside an extended submission file list.",
    )
    suspend fun getReferencedFiles(
        @PathVariable accNo: String,
        fileListPath: FileListPath,
    ): ExtFileTable = extSubmissionQueryService.getReferencedFiles(accNo, fileListPath.path)

    @GetMapping("/{accNo}/fileList/{*path}")
    @Operation(
        summary = "Page Extended File List",
        description = "Return a paginated view of files in one extended submission file list.",
    )
    suspend fun fileListFiles(
        @PathVariable accNo: String,
        @PathVariable path: String,
        @ModelAttribute request: ExtPageRequest,
    ): WebExtPage<ExtFile> {
        val pageRequest = ExtFileListFilesRequest(accNo, path.removePrefix("/"), request)
        val files = extSubmissionQueryService.getReferencedFiles(pageRequest)
        return extendedFilePageMapper.asExtPage(files, pageRequest)
    }

    @GetMapping("/{accNo}/linkList/{*linkListName}")
    @Operation(
        summary = "Page Extended Link List",
        description = "Return a paginated view of links in one extended submission link list.",
    )
    suspend fun linkListLinks(
        @PathVariable accNo: String,
        @PathVariable linkListName: String,
        @ModelAttribute request: ExtPageRequest,
    ): WebExtPage<ExtLink> {
        val pageRequest = ExtLinkListFilesRequest(accNo, linkListName.removePrefix("/"), request)
        val links = extSubmissionQueryService.getReferencedLinks(pageRequest)
        return extendedLinkPageMapper.asExtPage(links, pageRequest)
    }

    @GetMapping
    @Operation(
        summary = "Search Extended Submissions",
        description = "Search extended submission documents using paging and filtering parameters.",
    )
    suspend fun submissions(
        @ModelAttribute request: ExtSubPageRequest,
    ): WebExtPage<ExtSubmission> {
        val submissions = extSubmissionQueryService.getExtendedSubmissions(request)
        return extPageMapper.asExtPage(submissions, request)
    }
}
