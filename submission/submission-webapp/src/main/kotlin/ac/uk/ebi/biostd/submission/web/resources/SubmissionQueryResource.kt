package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.ListFilter
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.submission.SubmissionQueryService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilterRequest
import ac.uk.ebi.biostd.submission.web.model.asFilter
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.security.integration.model.api.SecurityUser
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
class SubmissionQueryResource(
    private val submissionService: SubmissionQueryService,
    private val submissionsWebHandler: SubmissionsWebHandler,
) {
    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    suspend fun asJson(
        @PathVariable accNo: String,
    ) = submissionService.getSubmission(accNo, SubFormat.JSON)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    suspend fun asTsv(
        @PathVariable accNo: String,
    ) = submissionService.getSubmission(accNo, SubFormat.TSV)

    @GetMapping("/{accNo}/{fileList}.tsv")
    suspend fun asTsv(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.TSV)

    @GetMapping("/{accNo}/{fileList}.json")
    suspend fun asJson(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.JSON)

    @GetMapping
    suspend fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute request: SubmissionFilterRequest,
    ): List<SubmissionDto> {
        val filter = request.asFilter(user.email, user.superuser, user.adminCollections)
        return submissionsWebHandler.getSubmissions(filter).map { it.asDto() }
    }

    @GetMapping("/{accNo}")
    suspend fun getSubmission(
        @PathVariable accNo: String,
        @BioUser user: SecurityUser,
    ): SubmissionDto? {
        val filter = ListFilter(user.email, user.superuser, accNo, user.adminCollections, limit = 1)
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
            accNo,
            title.orEmpty(),
            modificationTime,
            releaseTime,
            status.value,
            errors,
        )
}
