package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.ContentSubmitWebRequest
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.RefreshWebRequest
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.ATTRIBUTES
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
class SubmitResource(private val submitWebHandler: SubmitWebHandler) {
    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_XML"],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun submitXml(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(
            submission = submission,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = XML,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = emptyList()
        )

        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$TEXT_PLAIN"],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun submitTsv(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(
            submission = submission,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = TSV,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = emptyList()
        )

        return submitWebHandler.submit(request)
    }

    @PostMapping(
        headers = ["$SUBMISSION_TYPE=$APPLICATION_JSON"],
        produces = [APPLICATION_JSON_VALUE]
    )
    @ResponseBody
    fun submitJson(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(FILE_MODE, defaultValue = "COPY") mode: FileMode,
        @RequestParam(ATTRIBUTES, required = false) attributes: Map<String, String>?,
        @RequestBody submission: String
    ): Submission {
        val request = ContentSubmitWebRequest(
            submission = submission,
            onBehalfRequest = onBehalfRequest,
            user = user,
            format = JSON_PRETTY,
            fileMode = mode,
            attrs = attributes.orEmpty(),
            files = emptyList()
        )

        return submitWebHandler.submit(request)
    }

    @PostMapping("/refresh/{accNo}")
    fun refreshSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String
    ): Submission = submitWebHandler.refreshSubmission(RefreshWebRequest(accNo, user))
}
