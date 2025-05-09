package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.files.web.common.FileListPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.extended.ExtPageRequest
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.WebExtPage
import ebi.ac.uk.model.SubmissionId
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
import java.time.Instant

@RestController
@RequestMapping("/submissions/extended")
class ExtSubmissionResource(
    private val extPageMapper: ExtendedPageMapper,
    private val extSubmissionService: ExtSubmissionService,
    private val extSubmissionQueryService: ExtSubmissionQueryService,
    private val extSerializationService: ExtSerializationService,
) {
    @GetMapping("/{accNo}")
    suspend fun getExtended(
        @PathVariable accNo: String,
        @RequestParam(name = "includeFileList", required = false) includeFileList: Boolean?,
    ): ExtSubmission = extSubmissionQueryService.getExtendedSubmission(accNo, includeFileList.orFalse())

    @GetMapping("/{accNo}/referencedFiles/**")
    suspend fun getReferencedFiles(
        @PathVariable accNo: String,
        fileListPath: FileListPath,
    ): ExtFileTable = extSubmissionQueryService.getReferencedFiles(accNo, fileListPath.path)

    @PostMapping("/re-trigger/{accNo}/{version}")
    suspend fun reTriggerSubmission(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): ExtSubmission = extSubmissionService.reTriggerSubmission(accNo, version)

    @PostMapping("/refresh/{accNo}")
    suspend fun refreshSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): Pair<String, Int> = extSubmissionService.refreshSubmission(user.email, accNo)

    @PostMapping("/release/{accNo}/{releaseDate}")
    suspend fun releaseSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
        @PathVariable releaseDate: String,
    ): Pair<String, Int> = extSubmissionService.releaseSubmission(user.email, accNo, Instant.parse(releaseDate))

    @PostMapping("/stats/refreshAll")
    suspend fun refreshAllStatus() {
        extSubmissionService.refreshAllStats()
    }

    @PostMapping("/{accNo}/transfer/{target}")
    suspend fun transferSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
        @PathVariable target: StorageMode,
    ) = extSubmissionService.transferSubmission(user.email, accNo, target)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    suspend fun submitExtended(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) extSubmission: String,
    ): ExtSubmission =
        extSubmissionService.submitExt(
            user.email,
            extSerializationService.deserialize(extSubmission),
        )

    @PostMapping("/async")
    @PreAuthorize("isAuthenticated()")
    suspend fun submitExtendedAsync(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) extSubmission: String,
    ): SubmissionId =
        extSubmissionService.submitExtAsync(
            user.email,
            extSerializationService.deserialize(extSubmission),
        )

    @GetMapping
    suspend fun submissions(
        @ModelAttribute request: ExtPageRequest,
    ): WebExtPage = extPageMapper.asExtPage(extSubmissionQueryService.getExtendedSubmissions(request), request)
}
