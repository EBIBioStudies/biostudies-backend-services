package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.model.RequestStatus.DRAFT
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import kotlinx.coroutines.flow.Flow
import mu.KotlinLogging
import java.time.Instant
import java.time.ZoneOffset.UTC

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class SubmissionRequestDraftService(
//    private val submitWebHandler: SubmitWebHandler,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val serializationService: SerializationService,
//    private val submitRequestBuilder: SubmitRequestBuilder,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submissionQueryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    suspend fun findActiveRequestDrafts(
        owner: String,
        pageRequest: PageRequest = PageRequest(),
    ): Flow<SubmissionRequest> {
        return requestService.findRequestDrafts(owner, pageRequest)
    }

    suspend fun getOrCreateRequestDraft(
        key: String,
        owner: String,
    ): SubmissionRequest {
        return requestService.findRequestDraft(key, owner)
            ?: requestService.findSubmissionRequestDraft(key)
            ?: createRequestDraftFromSubmission(key, owner)
    }

    suspend fun getRequestDraft(
        key: String,
        owner: String,
    ): String {
        return getOrCreateRequestDraft(key, owner).draft
    }

    suspend fun deleteRequestDraft(
        key: String,
        owner: String,
    ) {
        requestService.deleteRequestDraft(key, owner)
        logger.info { "$key $owner Draft with key '$key' DELETED for user '$owner'" }
    }

    suspend fun updateRequestDraft(
        key: String,
        owner: String,
        draft: String,
    ): SubmissionRequest {
        val requestDraft = getOrCreateRequestDraft(key, owner)
        val modificationTime = Instant.now()

        requestService.updateRequestDraft(requestDraft.key, owner, draft, modificationTime)
        logger.info { "$key $owner Draft with key '$key' UPDATED for user '$owner'" }

        return requestDraft.copy(draft = draft, modificationTime = modificationTime.atOffset(UTC))
    }

    suspend fun createRequestDraft(
        draft: String,
        owner: String,
    ): SubmissionRequest {
        return createActiveRequestDraft(draft, owner)
    }

    suspend fun createRequestDraftFromSubmission(
        accNo: String,
        owner: String,
    ): SubmissionRequest {
        require(userPrivilegesService.canResubmit(owner, accNo)) { throw UserCanNotUpdateSubmit(accNo, owner) }
        val submission = toSubmissionMapper.toSimpleSubmission(submissionQueryService.getExtByAccNo(accNo))
        val draft = serializationService.serializeSubmission(submission, JsonPretty)

        return createActiveRequestDraft(draft, owner, accNo)
    }

    private suspend fun createActiveRequestDraft(
        draft: String,
        owner: String,
        accNo: String? = null,
    ): SubmissionRequest {
        val creationTime = Instant.now()
        val key = "TMP_$creationTime"
        val request =
            SubmissionRequest(
                key,
                accNo = accNo ?: key,
                version = 0,
                owner = owner,
                draft = draft,
                status = DRAFT,
                modificationTime = creationTime.atOffset(UTC),
            )

        requestService.createRequest(request)

        return request
    }

//    suspend fun submitRequestDraft(
//        key: String,
//        user: SecurityUser,
//        onBehalfRequest: OnBehalfParameters?,
//        parameters: SubmitParameters,
//    ) {
//        val submission = getRequestDraft(key, user.email)
//        val buildRequest = SubmitBuilderRequest(user, onBehalfRequest, parameters, key, submission)
//        val request = submitRequestBuilder.buildContentRequest(submission, JSON_PRETTY, buildRequest)
//        submitWebHandler.submitAsync(request)
//        logger.info { "$key ${user.email} Draft with key '$key' SUBMITTED" }
//    }
}
