package ac.uk.ebi.biostd.submission.web.resources.internal.misc

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.RefreshWebRequest
import ebi.ac.uk.api.TOKEN_HEADER
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submissions"])
@ApiIgnore
@Suppress("LongParameterList")
class RefreshResource(private val submitWebHandler: SubmitWebHandler) {

    @PostMapping("/refresh/{accNo}")
    @ApiOperation("Update submission based on system db stored information")
    @ApiImplicitParam(name = TOKEN_HEADER, value = "User auth token", required = true, paramType = "header")
    fun refreshSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "accNo", value = "The accession number of the submission to be refresh")
        @PathVariable accNo: String
    ): Submission {
        return submitWebHandler.refreshSubmission(RefreshWebRequest(accNo, user))
    }
}
