package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.model.SubmissionDraft
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.web.handlers.SubmitBuilderRequest
import ac.uk.ebi.biostd.submission.web.handlers.SubmitRequestBuilder
import ac.uk.ebi.biostd.submission.web.handlers.SubmitWebHandler
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import java.time.Clock
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionDraftService(
    private val clock: Clock,
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
        filter: PageRequest = PageRequest(),
    ): Flow<SubmissionDraft> = draftPersistenceService.getActiveSubmissionDrafts(userEmail, filter)

    suspend fun getOrCreateSubmissionDraft(
        userEmail: String,
        key: String,
    ): SubmissionDraft = draftPersistenceService.findSubmissionDraft(userEmail, key) ?: createDraftFromSubmission(userEmail, key)

    suspend fun getSubmissionDraftContent(
        userEmail: String,
        key: String,
    ): String = getOrCreateSubmissionDraft(userEmail, key).content

    suspend fun deleteSubmissionDraft(
        userEmail: String,
        key: String,
    ) {
        draftPersistenceService.deleteSubmissionDraft(userEmail, key)
        logger.info { "$key $userEmail Draft with key '$key' DELETED for user '$userEmail'" }
    }

    suspend fun updateSubmissionDraft(
        userEmail: String,
        key: String,
        content: String,
    ): SubmissionDraft {
        val updated = draftPersistenceService.updateSubmissionDraft(userEmail, key, content, Instant.now(clock))
        logger.info { "$key $userEmail Draft with key '$key' UPDATED for user '$userEmail'" }

        return updated
    }

    suspend fun createSubmissionDraft(
        userEmail: String,
        content: String,
    ): SubmissionDraft {
        val creationTime = Instant.now(clock)
        val draftKey = "TMP_$creationTime"
        val draft = draftPersistenceService.createSubmissionDraft(userEmail, draftKey, content, creationTime)
        logger.info { "$draftKey $userEmail Draft with key '$draftKey' CREATED for user '$userEmail'" }

        return draft
    }

    suspend fun submitDraftAsync(
        key: String,
        user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        parameters: SubmitParameters,
    ) {
        val submission = getOrCreateSubmissionDraft(user.email, key).content
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, key, submission)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.JSON_PRETTY, buildRequest)
        submitWebHandler.submitAsync(request)
        logger.info { "$key ${user.email} Draft with key '$key' SUBMITTED" }
    }

    suspend fun submitDraftSync(
        key: String,
        user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        parameters: SubmitParameters,
    ): Submission {
        val submission = getOrCreateSubmissionDraft(user.email, key).content
        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, key, submission)
        val request = submitRequestBuilder.buildContentRequest(submission, SubFormat.JSON_PRETTY, buildRequest)

        logger.info { "$key ${user.email} Started SUBMITTED process draft with key '$key'" }

        return submitWebHandler.submit(request)
    }

    private suspend fun createDraftFromSubmission(
        userEmail: String,
        accNo: String,
    ): SubmissionDraft {
        require(userPrivilegesService.canResubmit(userEmail, accNo)) { throw UserCanNotUpdateSubmit(accNo, userEmail) }
        val submission = toSubmissionMapper.toSimpleSubmission(submissionQueryService.getExtByAccNo(accNo))
        val content = serializationService.serializeSubmission(submission, JsonPretty)
        return draftPersistenceService.createSubmissionDraft(userEmail, accNo, content, Instant.now(clock))
    }
}
