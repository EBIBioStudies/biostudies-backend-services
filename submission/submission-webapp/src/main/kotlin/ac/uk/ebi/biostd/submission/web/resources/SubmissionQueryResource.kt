package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionListFilter
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilterRequest
import ac.uk.ebi.biostd.submission.web.model.asFilter
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@Tag(name = "Submissions", description = "Search submissions and retrieve released or user-visible submission content.")
class SubmissionQueryResource(
    private val submissionService: SubmissionQueryService,
    private val submissionsWebHandler: SubmissionsWebHandler,
) {
    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    @Operation(
        summary = "Get Submission JSON",
        description = "Retrieve a released submission by accession number in BioStudies JSON format.",
    )
    suspend fun asJson(
        @PathVariable accNo: String,
    ) = submissionService.getSubmission(accNo, SubFormat.JSON)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    @Operation(
        summary = "Get Submission PageTab",
        description = "Retrieve a released submission by accession number in PageTab TSV format.",
    )
    suspend fun asTsv(
        @PathVariable accNo: String,
    ) = submissionService.getSubmission(accNo, SubFormat.TSV)

    @GetMapping("/{accNo}/{fileList}.tsv")
    @Operation(
        summary = "Download File List TSV",
        description = "Download one named file list from a released submission in TSV format.",
    )
    suspend fun asTsv(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.TSV)

    @GetMapping("/{accNo}/{fileList}.json")
    @Operation(
        summary = "Download File List JSON",
        description = "Download one named file list from a released submission in JSON format.",
    )
    suspend fun asJson(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.JSON)

    @GetMapping
    @Operation(
        summary = "Search My Submissions",
        description =
            "Search submissions visible to the authenticated user. Results are filtered by ownership, " +
                "superuser permissions, and administrated collections.",
    )
    suspend fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute request: SubmissionFilterRequest,
    ): List<SubmissionDto> {
        val filter = request.asFilter(user.email, user.superuser, user.adminCollections)
        return submissionsWebHandler.getSubmissions(filter).map { it.asDto() }
    }

    @GetMapping("/{accNo}")
    @Operation(
        summary = "Get User-Visible Submission Summary",
        description =
            "Return summary metadata for a submission if it is visible to the authenticated user. " +
                "Use the format-specific endpoints to retrieve released submission content.",
    )
    suspend fun getSubmission(
        @PathVariable accNo: String,
        @BioUser user: SecurityUser,
    ): SubmissionDto? {
        val filter = SubmissionListFilter(user.email, user.superuser, accNo, user.adminCollections, limit = 1)
        return submissionsWebHandler.getSubmissions(filter).firstOrNull()?.let { it.asDto() }
    }

    private suspend fun fileListFile(
        accNo: String,
        fileListName: String,
        subFormat: SubFormat,
    ): ResponseEntity<Resource> {
        val fileList = submissionService.getFileList(accNo, fileListName, subFormat)
        val resource = InputStreamResource(fileList.inputStream())
        return ResponseEntity
            .ok()
            .contentLength(fileList.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    private fun BasicSubmission.asDto() =
        SubmissionDto(
            accno = accNo,
            displayAccNo = displayAccNo,
            newSubmission = newSubmission,
            title = title.orEmpty(),
            mtime = modificationTime,
            rtime = releaseTime,
            status = status.value,
            errors = errors,
        )
}
