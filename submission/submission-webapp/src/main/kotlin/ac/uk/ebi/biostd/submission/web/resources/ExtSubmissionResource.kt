package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ebi.ac.uk.extended.model.ExtSubmission
import io.swagger.annotations.Api
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Extended Submissions"])
class ExtSubmissionResource(private val extSubmissionService: ExtSubmissionService) {
    @GetMapping("/{accNo}")
    fun getExtended(@PathVariable accNo: String): ExtSubmission = extSubmissionService.getExtendedSubmission(accNo)

    @PostMapping
    fun submitExtended(
        @RequestBody extSubmission: ExtSubmission
    ): ExtSubmission = extSubmissionService.submitExtendedSubmission(extSubmission)
}
