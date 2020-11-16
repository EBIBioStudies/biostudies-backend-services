package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SimpleSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiImplicitParams
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submissions"])
class SubmissionResource(
    private val submissionsWebHandler: SubmissionsWebHandler
) {

    @GetMapping
    @ApiOperation("Get the basic data for the submissions that matches the given filter")
    @ApiImplicitParams(value = [
        ApiImplicitParam(
            name = "X-Session-Token",
            value = "User authentication token",
            required = true,
            paramType = "header"),
        ApiImplicitParam(
            name = "limit",
            value = "Optional query parameter used to set the maximum amount of drafts in the response"),
        ApiImplicitParam(
            name = "offset",
            value = "Optional query parameter used to indicate from which submission should the response start")
    ])
    fun getSubmissions(
        @BioUser user: SecurityUser,
        @ModelAttribute filter: SubmissionFilter
    ): List<SubmissionDto> = submissionsWebHandler.getSubmissions(user, filter).map { it.asDto() }

    @DeleteMapping("/{accNo}")
    @ApiOperation("Delete the submission with the given accession number")
    @ApiImplicitParam(
        name = "X-Session-Token",
        value = "User authentication token",
        required = true,
        paramType = "header")
    fun deleteSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "accNo", value = "The accession number of the submission to be deleted")
        @PathVariable accNo: String
    ): Unit = submissionsWebHandler.deleteSubmission(accNo, user)

    private fun SimpleSubmission.asDto() =
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
