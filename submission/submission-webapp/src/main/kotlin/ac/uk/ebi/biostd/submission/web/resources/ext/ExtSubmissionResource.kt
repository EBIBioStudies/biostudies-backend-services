package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.SubmissionTransferOptions
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.Instant

@RestController
@RequestMapping("/submissions/extended")
@Tag(
    name = "Extended Submission Operations",
    description = "Operational actions for extended submissions and backend maintenance workflows.",
)
@Suppress("TooManyFunctions")
class ExtSubmissionResource(
    private val extSubmissionService: ExtSubmissionService,
    private val extSubmissionQueryService: ExtSubmissionQueryService,
    private val extSerializationService: ExtSerializationService,
) {
    @GetMapping("/{accNo}")
    @Operation(
        summary = "Get Extended Submission",
        description = "Return the extended submission document for an accession number, optionally including file-list details.",
    )
    suspend fun getExtended(
        @PathVariable accNo: String,
        @RequestParam(name = "includeFileList", required = false) includeFileList: Boolean?,
    ): ExtSubmission = extSubmissionQueryService.getExtendedSubmission(accNo, includeFileList.orFalse())

    @PostMapping("/re-trigger/{accNo}/{version}")
    @Operation(
        summary = "Re-trigger Submission Processing",
        description = "Re-run processing for a specific submission accession and version.",
    )
    suspend fun reTriggerSubmission(
        @PathVariable accNo: String,
        @PathVariable version: Int,
    ): ExtSubmission = extSubmissionService.reTriggerSubmission(accNo, version)

    @PostMapping("/refresh/{accNo}")
    @Operation(
        summary = "Refresh Submission",
        description = "Refresh persisted extended submission data for an accession number.",
    )
    suspend fun refreshSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): SubmissionId = extSubmissionService.refreshSubmission(user.email, accNo)

    @PostMapping("/reindex/{accNo}")
    suspend fun reindexSubmission(
        @PathVariable accNo: String,
    ) = extSubmissionService.reindexSubmission(accNo)

    @PostMapping("/release/{accNo}/{releaseDate}")
    @Operation(
        summary = "Release Submission",
        description = "Set a submission release date and trigger release handling.",
    )
    suspend fun releaseSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
        @PathVariable releaseDate: String,
    ): SubmissionId = extSubmissionService.releaseSubmission(user.email, accNo, Instant.parse(releaseDate))

    @PostMapping("/{accNo}/generate-doi")
    @Operation(
        summary = "Generate DOI",
        description = "Generate or request a DOI for a submission accession.",
    )
    suspend fun generateDoi(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
    ): SubmissionId = extSubmissionService.generateDoi(user.email, accNo)

    @PostMapping("/{accNo}/migrate/{target}")
    @Operation(
        summary = "Migrate Submission Storage",
        description = "Move a submission to the requested storage mode.",
    )
    suspend fun migrateSubmission(
        @BioUser user: SecurityUser,
        @PathVariable accNo: String,
        @PathVariable target: StorageMode,
    ) = extSubmissionService.migrateSubmission(user.email, accNo, target)

    @PostMapping("/transfer")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Transfer Submissions",
        description = "Transfer ownership or collection context for a group of submissions.",
    )
    suspend fun transferSubmissions(
        @BioUser user: SecurityUser,
        @RequestBody options: SubmissionTransferOptions,
    ) = extSubmissionService.transferSubmissions(user.email, options)

    @PostMapping("/transfer/email-update")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update Transfer Emails",
        description = "Update email metadata as part of a submission transfer operation.",
    )
    suspend fun update(
        @BioUser user: SecurityUser,
        @RequestBody options: SubmissionTransferOptions,
    ) = extSubmissionService.transferEmailUpdate(user.email, options)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Submit Extended Submission",
        description = "Submit an already serialized extended submission and return the stored extended document.",
    )
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
    @Operation(
        summary = "Submit Extended Submission Asynchronously",
        description = "Submit an already serialized extended submission for background processing.",
    )
    suspend fun submitExtendedAsync(
        @BioUser user: SecurityUser,
        @RequestParam(SUBMISSION) extSubmission: String,
    ): SubmissionId =
        extSubmissionService.submitExtAsync(
            user.email,
            extSerializationService.deserialize(extSubmission),
        )
}
