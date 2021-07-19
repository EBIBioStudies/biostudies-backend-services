package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/extended")
class ExtSubmissionResource(
    private val extPageMapper: ExtendedPageMapper,
    private val extSubmissionService: ExtSubmissionService
) {
    @GetMapping("/{accNo}")
    fun getExtended(@PathVariable accNo: String): ExtSubmission = extSubmissionService.getExtendedSubmission(accNo)

    @GetMapping("/{accNo}/fileList/{fileListName}/files")
    fun getReferencedFiles(
        @PathVariable accNo: String,
        @PathVariable fileListName: String
    ): List<ExtFile> = extSubmissionService.getReferencedFiles(accNo, fileListName)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun submitExtended(
        @BioUser user: SecurityUser,
        @RequestBody extSubmission: ExtSubmission
    ): ExtSubmission = extSubmissionService.submitExtendedSubmission(user.email, extSubmission)

    @GetMapping
    fun submissions(@ModelAttribute request: ExtPageRequest): ExtPage =
        extPageMapper.asExtPage(extSubmissionService.getExtendedSubmissions(request), request)
}
