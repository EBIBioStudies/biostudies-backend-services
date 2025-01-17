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

@Suppress("LongParameterList", "TooManyFunctions")
class SubmissionRequestDraftService(
    private val toSubmissionMapper: ToSubmissionMapper,
    private val serializationService: SerializationService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val submissionQueryService: SubmissionPersistenceQueryService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    suspend fun findActiveRequestDrafts(
        owner: String,
        pageRequest: PageRequest = PageRequest(),
    ): Flow<SubmissionRequest> = requestService.findRequestDrafts(owner, pageRequest)

    suspend fun hasProcessingRequest(accNo: String): Boolean = requestService.hasActiveRequest(accNo)

    suspend fun getOrCreateRequestDraftFromSubmission(
        accNo: String,
        owner: String,
    ): SubmissionRequest =
        requestService.findRequestDraft(accNo, owner)
            ?: createRequestDraftFromSubmission(accNo, owner)

    suspend fun getOrCreateRequestDraft(
        accNo: String,
        owner: String,
        draft: String,
    ): SubmissionRequest =
        requestService.findRequestDraft(accNo, owner)
            ?: createActiveRequestDraft(draft, owner, accNo)

    suspend fun getRequestDraft(
        accNo: String,
        owner: String,
    ): SubmissionRequest = getOrCreateRequestDraftFromSubmission(accNo, owner)

    suspend fun deleteRequestDraft(
        accNo: String,
        owner: String,
    ) {
        requestService.deleteRequestDraft(accNo, owner)
        logger.info { "$accNo $owner Draft with key '$accNo' DELETED for user '$owner'" }
    }

    suspend fun updateRequestDraft(
        accNo: String,
        owner: String,
        draft: String,
    ): SubmissionRequest {
        val requestDraft = getOrCreateRequestDraftFromSubmission(accNo, owner)
        val modificationTime = Instant.now()

        requestService.updateRequestDraft(requestDraft.accNo, owner, draft, modificationTime)
        logger.info { "$accNo $owner Draft with key '$accNo' UPDATED for user '$owner'" }

        return requestDraft.copy(draft = draft, modificationTime = modificationTime.atOffset(UTC))
    }

    suspend fun createRequestDraft(
        draft: String,
        owner: String,
    ): SubmissionRequest = createActiveRequestDraft(draft, owner)

    suspend fun setSubRequestAccNo(
        tempAccNo: String,
        accNo: String,
        owner: String,
    ) = requestService.setSubRequestAccNo(tempAccNo, accNo, owner, Instant.now())

    private suspend fun createRequestDraftFromSubmission(
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
        val request =
            SubmissionRequest(
                accNo = accNo ?: "TMP_$creationTime",
                version = 0,
                owner = owner,
                draft = draft,
                status = DRAFT,
                modificationTime = creationTime.atOffset(UTC),
            )

        requestService.saveRequest(request)

        return request
    }
}
