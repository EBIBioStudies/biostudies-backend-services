package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ebi.ac.uk.extended.model.ExtSubmission
import io.swagger.annotations.Api
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Extended Submissions"])
class ExtSubmissionResource(private val extSubmissionService: ExtSubmissionService) {
    @GetMapping("/{accNo}/extended")
    fun getExtended(@PathVariable accNo: String): ExtSubmission = extSubmissionService.getExtendedSubmission(accNo)
}
