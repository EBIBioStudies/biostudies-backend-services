package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ebi.ac.uk.model.constants.APPLICATION_JSON
import ebi.ac.uk.model.constants.TEXT_PLAIN
import ebi.ac.uk.model.constants.TEXT_XML
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@Api(tags = ["Submissions"])
class SubmissionsResource(
    private val submissionService: SubmissionService
) {

    @GetMapping("/{accNo}.json", produces = [APPLICATION_JSON])
    @ResponseBody
    @ApiOperation("Get the JSON file for the submission with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun asJson(
        @ApiParam(name = "accNo", value = "The submission accession number")
        @PathVariable accNo: String
    ) = submissionService.getSubmissionAsJson(accNo)

    @GetMapping("/{accNo}.xml", produces = [TEXT_XML])
    @ApiOperation("Get the XML file for the submission with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun asXml(
        @ApiParam(name = "accNo", value = "The submission accession number")
        @PathVariable accNo: String
    ) = submissionService.getSubmissionAsXml(accNo)

    @GetMapping("/{accNo}.tsv", produces = [TEXT_PLAIN])
    @ApiOperation("Get the TSV file for the submission with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun asTsv(
        @ApiParam(name = "accNo", value = "The submission accession number")
        @PathVariable accNo: String
    ) = submissionService.getSubmissionAsTsv(accNo)
}
