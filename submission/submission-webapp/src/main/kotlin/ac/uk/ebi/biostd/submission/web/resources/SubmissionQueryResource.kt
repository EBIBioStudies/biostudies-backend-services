package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionQueryService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ac.uk.ebi.biostd.submission.web.model.SubmissionFilterRequest
import ac.uk.ebi.biostd.submission.web.model.asFilter
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files

@RestController
@RequestMapping("/submissions")
class SubmissionQueryResource(
    private val submissionService: SubmissionQueryService,
    private val submissionsWebHandler: SubmissionsWebHandler,
) {
    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    suspend fun asJson(@PathVariable accNo: String) = submissionService.getSubmission(accNo, SubFormat.JSON)

    @GetMapping("/{accNo}.xml", produces = [TEXT_XML])
    suspend fun asXml(@PathVariable accNo: String) = submissionService.getSubmission(accNo, SubFormat.XML)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    suspend fun asTsv(@PathVariable accNo: String) = submissionService.getSubmission(accNo, SubFormat.TSV)

    @GetMapping("/{accNo}/{fileList}.tsv")
    suspend fun asTsv(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.TSV)

    @GetMapping("/{accNo}/{fileList}.xml")
    suspend fun asXml(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.XML)

    @GetMapping("/{accNo}/{fileList}.json")
    suspend fun asJson(
        @PathVariable accNo: String,
        @PathVariable fileList: String,
    ): ResponseEntity<Resource> = fileListFile(accNo, fileList, SubFormat.JSON)

    @GetMapping
    suspend fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute request: SubmissionFilterRequest,
    ): List<SubmissionDto> = submissionsWebHandler.getSubmissions(user, request.asFilter()).map { it.asDto() }

    private suspend fun fileListFile(
        accNo: String,
        fileListName: String,
        subFormat: SubFormat,
    ): ResponseEntity<Resource> {
        val fileList = submissionService.getFileList(accNo, fileListName, subFormat)
        val resource = ByteArrayResource(Files.readAllBytes(fileList.toPath()))
        return ResponseEntity.ok()
            .contentLength(fileList.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    private fun BasicSubmission.asDto() =
        SubmissionDto(
            accNo,
            title.orEmpty(),
            version,
            creationTime,
            modificationTime,
            releaseTime,
            method,
            status.value
        )
}
