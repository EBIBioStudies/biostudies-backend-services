package ac.uk.ebi.biostd.submission.web.resources.submit.async

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.stats.web.TempFileGenerator
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.model.AcceptedSubmission
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/submissions/async")
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
class MultipartAsyncSubmitResource(
    private val submitWebHandler: SubmitWebHandler,
    private val submitRequestBuilder: SubmitRequestBuilder,
    private val tempFileGenerator: TempFileGenerator,
) {
    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON_VALUE"],
        produces = [APPLICATION_JSON_VALUE],
    )
    suspend fun submitMultipartJson(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestParam(SUBMISSION) content: String,
        @RequestParam(FILES, required = false) files: List<MultipartFile>?,
        @ModelAttribute parameters: SubmitParameters,
    ): AcceptedSubmission {
        val subFiles = tempFileGenerator.asFiles(files.orEmpty())
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, files = subFiles)
        val contentWebRequest = submitRequestBuilder.buildContentRequest(content, SubFormat.JSON, buildRequest)

        return submitWebHandler.submitAsync(contentWebRequest)
    }

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE],
    )
    suspend fun submitMultipartTsv(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestParam(SUBMISSION) content: String,
        @RequestParam(FILES, required = false) files: List<MultipartFile>?,
        @ModelAttribute parameters: SubmitParameters,
    ): AcceptedSubmission {
        val subFiles = tempFileGenerator.asFiles(files.orEmpty())
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, files = subFiles)
        val contentWebRequest = submitRequestBuilder.buildContentRequest(content, SubFormat.TSV, buildRequest)

        return submitWebHandler.submitAsync(contentWebRequest)
    }

    @PostMapping(
        value = ["/direct"],
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"],
        produces = [APPLICATION_JSON_VALUE],
    )
    suspend fun submitFile(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestParam(SUBMISSION) file: MultipartFile,
        @RequestParam(FILES, required = false) files: List<MultipartFile>?,
        @ModelAttribute parameters: SubmitParameters,
    ): AcceptedSubmission {
        val subFiles = tempFileGenerator.asFiles(files.orEmpty())
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, files = subFiles)
        val fileWebRequest = submitRequestBuilder.buildFileRequest(tempFileGenerator.asFile(file), buildRequest)

        return submitWebHandler.submitAsync(fileWebRequest)
    }
}
