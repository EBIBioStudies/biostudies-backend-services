package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionsWebHandler
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Submissions"])
class SubmissionResource(
    private val submissionService: SubmissionService,
    private val submissionsWebHandler: SubmissionsWebHandler
) {
    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    @ApiOperation("Get the JSON file for the submission with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun asJson(
        @ApiParam(name = "AccNo", value = "The submission accession number")
        @PathVariable accNo: String
    ) = submissionService.getSubmissionAsJson(accNo)

    @GetMapping("/{accNo}.xml", produces = [TEXT_XML])
    @ApiOperation("Get the XML file for the submission with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun asXml(
        @ApiParam(name = "AccNo", value = "The submission accession number")
        @PathVariable accNo: String
    ) = submissionService.getSubmissionAsXml(accNo)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    @ApiOperation("Get the TSV file for the submission with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun asTsv(
        @ApiParam(name = "AccNo", value = "The submission accession number")
        @PathVariable accNo: String
    ) = submissionService.getSubmissionAsTsv(accNo)

    @GetMapping
    @ApiOperation("Get the basic data for the submissions that matches the given filter")
    @ApiImplicitParams(value = [
        ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true),
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
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun deleteSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "AccNo", value = "The accession number of the submission to be deleted")
        @PathVariable accNo: String
    ): Unit = submissionsWebHandler.deleteSubmission(accNo, user)

    @PostMapping("refresh/{accNo}")
    @ApiOperation("Update submission based on system db stored information")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun refreshSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "AccNo", value = "The accession number of the submission to be refresh")
        @PathVariable accNo: String
    ) {
        submissionsWebHandler.refreshSubmission(accNo, user)
    }

    private fun SimpleSubmission.asDto() =
        SubmissionDto(accNo, title.orEmpty(), version, creationTime, modificationTime, releaseTime, method)
}
