package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.file.ExcelReader
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Submission Utilities", description = "Authenticated helper operations for validating, converting, and managing submissions.")
class SubmissionOperationsResource(
    private val submissionsWebHandler: SubmissionsWebHandler,
    private val submissionReleaser: SubmissionRequestReleaser,
    private val serializationService: SerializationService,
    private val tempFileGenerator: TempFileGenerator,
) {
    @PostMapping("/check-pagetab")
    @Operation(
        summary = "Convert Submission Content",
        description = "Validate and convert submission content between supported formats such as PageTab TSV and JSON.",
    )
    suspend fun checkSubmission(
        @RequestBody body: String,
        @RequestParam(name = "source") source: String,
        @RequestParam(name = "target") target: String,
    ): String {
        val submission = serializationService.deserializeSubmission(body, SubFormat.fromString(source))
        return serializationService.serializeSubmission(submission, SubFormat.fromString(target))
    }

    @PostMapping("/check-pagetab-file-list")
    @Operation(
        summary = "Convert File List Content",
        description = "Validate and convert file-list content between supported formats.",
    )
    suspend fun checkFileList(
        @RequestBody body: String,
        @RequestParam(name = "source") source: String,
        @RequestParam(name = "target") target: String,
    ): ResponseEntity<String> {
        val response = ByteArrayOutputStream()
        val files = serializationService.deserializeFileListAsFlow(body.byteInputStream(), SubFormat.fromString(source))
        serializationService.serializeFileList(files, SubFormat.fromString(target), response)
        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(response.toString(Charset.defaultCharset()))
    }

    @PostMapping("/check-pagetab-file-list-xlsx")
    @Operation(
        summary = "Convert Excel File List",
        description = "Read an uploaded Excel file list and return it in the requested serialized format.",
    )
    suspend fun checkFileListXlsx(
        @RequestParam(name = "source") source: MultipartFile,
        @RequestParam(name = "target") target: String,
    ): ResponseEntity<String> {
        val response = ByteArrayOutputStream()
        val excelFile = tempFileGenerator.asFile(source)
        val asTsv = ExcelReader.asTsv(excelFile)

        asTsv.inputStream().use { fileInputStream ->
            val files = serializationService.deserializeFileListAsFlow(fileInputStream, SubFormat.TSV)
            serializationService.serializeFileList(files, SubFormat.fromString(target), response)
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(response.toString(Charset.defaultCharset()))
    }

    @PostMapping("/ftp/generate")
    @Operation(
        summary = "Generate FTP Links",
        description = "Generate FTP links for a submission after its files have been prepared for release.",
    )
    suspend fun generateFtpLinks(
        @RequestParam("accNo", required = true) accNo: String,
    ) {
        submissionReleaser.generateFtpLinks(accNo)
    }

    @DeleteMapping("/{accNo}")
    @Operation(
        summary = "Delete Submission",
        description = "Delete a submission the authenticated user is allowed to manage.",
    )
    suspend fun deleteSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): Unit = submissionsWebHandler.deleteSubmission(accNo, user)

    @DeleteMapping
    @Operation(
        summary = "Delete Submissions",
        description = "Delete multiple submissions the authenticated user is allowed to manage.",
    )
    suspend fun deleteSubmissions(
        @BioUser user: SecurityUser,
        @RequestParam submissions: List<String>,
    ): Unit = submissionsWebHandler.deleteSubmissions(submissions, user)
}
