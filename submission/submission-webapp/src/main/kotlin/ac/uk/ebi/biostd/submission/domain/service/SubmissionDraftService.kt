package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ac.uk.ebi.biostd.submission.web.model.SubmissionRequestParameters
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.time.Instant

@Suppress("LongParameterList")
class SubmissionDraftService(
    private val submitWebHandler: SubmitWebHandler,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val serializationService: SerializationService,
    private val submitRequestBuilder: SubmitRequestBuilder,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submissionQueryService: SubmissionPersistenceQueryService,
    private val draftPersistenceService: SubmissionDraftPersistenceService,
) {
    fun getActiveSubmissionDrafts(
        userEmail: String,
        filter: PaginationFilter = PaginationFilter(),
    ): List<SubmissionDraft> {
        return draftPersistenceService.getActiveSubmissionDrafts(userEmail, filter)
    }

    fun getOrCreateSubmissionDraft(userEmail: String, key: String): SubmissionDraft {
        return draftPersistenceService.findSubmissionDraft(userEmail, key) ?: createDraftFromSubmission(userEmail, key)
    }

    fun getSubmissionDraftContent(userEmail: String, key: String): String {
        return getOrCreateSubmissionDraft(userEmail, key).content
    }

    fun deleteSubmissionDraft(userEmail: String, key: String) {
        draftPersistenceService.deleteSubmissionDraft(userEmail, key)
    }

    fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        return draftPersistenceService.updateSubmissionDraft(userEmail, key, content)
    }

    fun createSubmissionDraft(userEmail: String, content: String): SubmissionDraft {
        return draftPersistenceService.createSubmissionDraft(userEmail, "TMP_${Instant.now().toEpochMilli()}", content)
    }

    fun submitDraft(
        key: String,
        user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        parameters: SubmissionRequestParameters,
    ) {
        val submission = getOrCreateSubmissionDraft(user.email, key).content
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, key)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.JSON_PRETTY, buildRequest)

        submitWebHandler.submitAsync(request)
    }

    private fun createDraftFromSubmission(userEmail: String, accNo: String): SubmissionDraft {
        require(userPrivilegesService.canResubmit(userEmail, accNo)) { throw UserCanNotUpdateSubmit(accNo, userEmail) }

        val submission = toSubmissionMapper.toSimpleSubmission(submissionQueryService.getExtByAccNo(accNo))
        val content = serializationService.serializeSubmission(submission, JsonPretty)

        return draftPersistenceService.createSubmissionDraft(userEmail, accNo, content)
    }
}
