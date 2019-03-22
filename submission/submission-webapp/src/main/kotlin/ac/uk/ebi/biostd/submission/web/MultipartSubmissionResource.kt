package ac.uk.ebi.biostd.submission.web

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat.JSON
import ac.uk.ebi.biostd.SubFormat.TSV
import ac.uk.ebi.biostd.SubFormat.XML
import ac.uk.ebi.biostd.submission.model.ResourceFile
import ac.uk.ebi.biostd.submission.service.SubmissionService
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
class MultipartSubmissionResource(
    private val submissionService: SubmissionService,
    private val serializationService: SerializationService
) {

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON"])
    @ResponseBody
    fun submitJson(
        @AuthenticationPrincipal user: User,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(SUBMISSION) submission: String
    ) = submissionService.submit(
        serializationService.deserializeSubmission(submission, JSON), user, getFiles(files), JSON)

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"])
    @ResponseBody
    fun submitXml(
        @AuthenticationPrincipal user: User,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(SUBMISSION) submission: String
    ) = submissionService.submit(
        serializationService.deserializeSubmission(submission, XML), user, getFiles(files), XML)

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"])
    @ResponseBody
    fun submitTsv(
        @AuthenticationPrincipal user: User,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(SUBMISSION) submission: String
    ) = submissionService.submit(
        serializationService.deserializeSubmission(submission, TSV), user, getFiles(files), TSV)

    private fun getFiles(files: Array<MultipartFile>) =
            files.map { ResourceFile(it.originalFilename!!, it.inputStream, it.size) }
}
