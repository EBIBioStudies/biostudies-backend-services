package ac.uk.ebi.biostd.submission.web

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat.JSON
import ac.uk.ebi.biostd.SubFormat.TSV
import ac.uk.ebi.biostd.SubFormat.XML
import ac.uk.ebi.biostd.submission.model.ResourceFile
import ac.uk.ebi.biostd.submission.service.SubmissionService
import ebi.ac.uk.model.User
import ebi.ac.uk.model.constants.JSON_TYPE
import ebi.ac.uk.model.constants.MULTIPART
import ebi.ac.uk.model.constants.SUB_FILES_PARAM
import ebi.ac.uk.model.constants.SUB_PARAM
import ebi.ac.uk.model.constants.SUB_TYPE_HEADER
import ebi.ac.uk.model.constants.TSV_TYPE
import ebi.ac.uk.model.constants.XML_TYPE
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

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART", "$SUB_TYPE_HEADER=$JSON_TYPE"])
    @ResponseBody
    fun submitJson(
        @AuthenticationPrincipal user: User,
        @RequestParam(SUB_FILES_PARAM) files: Array<MultipartFile>,
        @RequestParam(SUB_PARAM) submission: String
    ) =
            submissionService.submit(serializationService.deserializeSubmission(submission, JSON), user, getFiles(files))

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART", "$SUB_TYPE_HEADER=$XML_TYPE"])
    @ResponseBody
    fun submitXml(
        @AuthenticationPrincipal user: User,
        @RequestParam(SUB_FILES_PARAM) files: Array<MultipartFile>,
        @RequestParam(SUB_PARAM) submission: String
    ) =
            submissionService.submit(serializationService.deserializeSubmission(submission, XML), user, getFiles(files))

    @PostMapping(headers = ["$CONTENT_TYPE=$MULTIPART", "$SUB_TYPE_HEADER=$TSV_TYPE"])
    @ResponseBody
    fun submitTsv(
        @AuthenticationPrincipal user: User,
        @RequestParam(SUB_FILES_PARAM) files: Array<MultipartFile>,
        @RequestParam(SUB_PARAM) submission: String
    ) =
            submissionService.submit(serializationService.deserializeSubmission(submission, TSV), user, getFiles(files))

    private fun getFiles(files: Array<MultipartFile>) = files.map { ResourceFile(it.originalFilename!!, it.inputStream) }
}
