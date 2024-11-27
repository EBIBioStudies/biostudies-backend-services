package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.SubmissionDraftService
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime

@RestController
@RequestMapping(value = ["submissions/drafts"], produces = [APPLICATION_JSON_VALUE])
@PreAuthorize("isAuthenticated()")
@Suppress("LongParameterList")
internal class SubmissionDraftResource(
    private val submissionDraftService: SubmissionDraftService,
) {
    @GetMapping
    @ResponseBody
    fun getSubmissionDrafts(
        @BioUser user: SecurityUser,
        @ModelAttribute filter: PageRequest,
    ): Flow<ResponseSubmissionDraft> = submissionDraftService.getActiveSubmissionDrafts(user.email, filter).map { it.asResponseDraft() }

    @GetMapping("/{key}")
    @ResponseBody
    suspend fun getOrCreateSubmissionDraft(
        @BioUser user: SecurityUser,
        @PathVariable key: String,
    ): ResponseSubmissionDraft = submissionDraftService.getOrCreateSubmissionDraft(user.email, key).asResponseDraft()

    @GetMapping("/{key}/content")
    @ResponseBody
    suspend fun getSubmissionDraftContent(
        @BioUser user: SecurityUser,
        @PathVariable key: String,
    ): ResponseSubmissionDraftContent = ResponseSubmissionDraftContent(submissionDraftService.getSubmissionDraftContent(user.email, key))

    @DeleteMapping("/{key}")
    suspend fun deleteSubmissionDraft(
        @BioUser user: SecurityUser,
        @PathVariable key: String,
    ) = submissionDraftService.deleteSubmissionDraft(user.email, key)

    @PutMapping("/{key}")
    @ResponseBody
    suspend fun updateSubmissionDraft(
        @BioUser user: SecurityUser,
        @RequestBody content: String,
        @PathVariable key: String,
    ): ResponseSubmissionDraft = submissionDraftService.updateSubmissionDraft(user.email, key, content).asResponseDraft()

    @PostMapping
    @ResponseBody
    suspend fun createSubmissionDraft(
        @BioUser user: SecurityUser,
        @RequestBody content: String,
    ): ResponseSubmissionDraft = submissionDraftService.createSubmissionDraft(user.email, content).asResponseDraft()

    @PostMapping("/{key}/submit")
    suspend fun submitDraft(
        @PathVariable key: String,
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ) {
        submissionDraftService.submitDraftAsync(key, user, onBehalfRequest, parameters)
    }

    @PostMapping("/{key}/submit/sync")
    suspend fun submitDraftSync(
        @PathVariable key: String,
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @ModelAttribute parameters: SubmitParameters,
    ): Submission = submissionDraftService.submitDraftSync(key, user, onBehalfRequest, parameters)
}

internal class ResponseSubmissionDraft(
    val key: String,
    @JsonRawValue val content: String,
    val modificationTime: OffsetDateTime,
)

internal class ResponseSubmissionDraftContent(
    @JsonRawValue @JsonValue val value: String,
)

internal fun SubmissionDraft.asResponseDraft() = ResponseSubmissionDraft(key, content, modificationTime)
