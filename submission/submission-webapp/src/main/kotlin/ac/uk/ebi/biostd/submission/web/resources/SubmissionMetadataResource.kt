package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.web.handlers.MetadataUpdateWebHandler
import ac.uk.ebi.biostd.submission.web.model.FileSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.extended.model.FileMode.MOVE
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
class SubmissionMetadataResource(
    private val tempFileGenerator: TempFileGenerator,
    private val metadataUpdateWebHandler: MetadataUpdateWebHandler
) {
    // TODO Add support for page tab content to be able to support
    // TODO Add documentation for the update metadata endpoints
    // TODO Add support for the update metadata operation in the CLI
    // TODO Unit and integration tests
    @PutMapping(
        value = ["/metadata/update"],
        headers = ["${HttpHeaders.CONTENT_TYPE}=$MULTIPART_FORM_DATA"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun updateMetadata(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(SUBMISSION) file: MultipartFile
    ): Submission {
        val subFile = tempFileGenerator.asFile(file)
        val updateMetadataRequest = FileSubmitWebRequest(
            submission = subFile,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = TSV,
            fileMode = MOVE,
            attrs = emptyMap(),
            files = emptyList()
        )

        return metadataUpdateWebHandler.updateMetadata(updateMetadataRequest)
    }
}
