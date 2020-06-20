package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
@Api(tags = ["Extended Submissions"])
class ExtSubmissionResource(private val extSubmissionService: ExtSubmissionService) {
    @GetMapping("/{accNo}")
    @ApiOperation("Get the extended model for a submission")
    @ApiImplicitParam(name = "X-SESSION-TOKEN", value = "User authentication token", required = true, type = "header")
    fun getExtended(
        @BioUser user: SecurityUser,

        @ApiParam(name = "accNo", value = "The submission accNo")
        @PathVariable accNo: String
    ): ExtSubmission = extSubmissionService.getExtendedSubmission(user.email, accNo)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiOperation("Make a submission using the extended model")
    @ApiImplicitParam(name = "X-SESSION-TOKEN", value = "User authentication token", required = true, type = "header")
    fun submitExtended(
        @BioUser user: SecurityUser,

        @ApiParam(name = "extSubmission", value = "The submission extended model representation")
        @RequestBody extSubmission: ExtSubmission
    ): ExtSubmission = extSubmissionService.submitExtendedSubmission(user.email, extSubmission)
}
