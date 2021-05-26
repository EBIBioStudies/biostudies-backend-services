package ac.uk.ebi.biostd.submission.web.resources.submit.async

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
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
    private val tempFileGenerator: TempFileGenerator
) {
    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$APPLICATION_JSON_VALUE"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun submitMultipartJson(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(SUBMISSION) content: String,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(FILES, required = false) files: Array<MultipartFile>?,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?
    ) {
        val tempFiles = tempFileGenerator.asFiles(files.orEmpty())
        val contentWebRequest = ContentSubmitWebRequest(
            submission = content,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = JSON,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = tempFiles
        )
        submitWebHandler.submitAsync(contentWebRequest)
    }

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun submitMultipartXml(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(SUBMISSION) content: String,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,
        @RequestParam(FILES, required = false) files: Array<MultipartFile>?
    ) {
        val tempFiles = tempFileGenerator.asFiles(files.orEmpty())
        val contentWebRequest = ContentSubmitWebRequest(
            submission = content,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = XML,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = tempFiles
        )
        submitWebHandler.submitAsync(contentWebRequest)
    }

    @PostMapping(
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA", "$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun submitMultipartTsv(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(SUBMISSION) content: String,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,
        @RequestParam(FILES, required = false) files: Array<MultipartFile>?
    ) {
        val tempFiles = tempFileGenerator.asFiles(files.orEmpty())
        val contentWebRequest = ContentSubmitWebRequest(
            submission = content,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = TSV,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = tempFiles
        )
        submitWebHandler.submitAsync(contentWebRequest)
    }

    @PostMapping(
        value = ["/direct"],
        headers = ["$CONTENT_TYPE=$MULTIPART_FORM_DATA"],
        produces = [APPLICATION_JSON_VALUE]
    )
    fun submitFile(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(SUBMISSION) file: MultipartFile,
        @RequestParam(FILES) files: Array<MultipartFile>,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam attributes: Map<String, String> = emptyMap()
    ) {
        val tempFiles = tempFileGenerator.asFiles(files)
        val subFile = tempFileGenerator.asFile(file)
        val contentWebRequest = FileSubmitWebRequest(
            submission = subFile,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = TSV,
            fileMode = mode,
            attrs = attributes,
            files = tempFiles
        )
        submitWebHandler.submitAsync(contentWebRequest)
    }
}
