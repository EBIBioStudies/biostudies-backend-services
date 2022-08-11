package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.files.web.common.FileListPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.WebExtPage
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@RestController
@RequestMapping("/submissions/extended")
class ExtSubmissionResource(
    private val extPageMapper: ExtendedPageMapper,
    private val extSubmissionService: ExtSubmissionService,
    private val extSubmissionQueryService: ExtSubmissionQueryService,
    private val extSerializationService: ExtSerializationService
) {
    @GetMapping("/{accNo}")
    fun getExtended(
        @PathVariable accNo: String,
        @RequestParam(name = "includeFileList", required = false) includeFileList: Boolean?
    ): ExtSubmission =
        extSubmissionQueryService.getExtendedSubmission(accNo, includeFileList.orFalse())

    @GetMapping("/{accNo}/referencedFiles/**")
    fun getReferencedFiles(
        @PathVariable accNo: String,
        fileListPath: FileListPath
    ): ExtFileTable = extSubmissionQueryService.getReferencedFiles(accNo, fileListPath.path)

    @PostMapping("/refresh/{accNo}")
    fun refreshSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String
    ): ExtSubmission = extSubmissionService.refreshSubmission(accNo, user.email)

    @PostMapping("/re-trigger/{accNo}/{version}")
    fun reTriggerSubmission(
        @PathVariable accNo: String,
        @PathVariable version: Int
    ): ExtSubmission = extSubmissionService.reTriggerSubmission(accNo, version)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun submitExtended(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) extSubmission: String
    ): ExtSubmission = extSubmissionService.submitExt(
        user.email,
        extSerializationService.deserialize(extSubmission),
    )

    @PostMapping("/async")
    @PreAuthorize("isAuthenticated()")
    fun submitExtendedAsync(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) extSubmission: String
    ) = extSubmissionService.submitExtAsync(
        user.email,
        extSerializationService.deserialize(extSubmission),
    )

    @GetMapping
    fun submissions(@ModelAttribute request: ExtPageRequest): WebExtPage =
        extPageMapper.asExtPage(extSubmissionQueryService.getExtendedSubmissions(request), request)
}
