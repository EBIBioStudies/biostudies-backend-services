package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentRequestDraftException
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.PageRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotUpdateSubmit
import ac.uk.ebi.biostd.submission.service.AccNoService
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.model.RequestStatus.DRAFT
import ebi.ac.uk.model.extensions.attachTo
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
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val requestService: SubmissionRequestPersistenceService,
    private val accNoService: AccNoService,
) {
    suspend fun findActiveRequestDrafts(
        owner: String,
        pageRequest: PageRequest = PageRequest(),
    ): Flow<SubmissionRequest> = requestService.findRequestDrafts(owner, pageRequest)

    suspend fun hasProcessingRequest(accNo: String): Boolean = requestService.hasProcessingRequest(accNo)

    suspend fun getOrCreateRequestDraftFromSubmission(
        accNo: String,
        owner: String,
    ): SubmissionRequest =
        requestService.findEditableRequest(accNo, owner)
            ?: createRequestDraftFromSubmission(accNo, owner)

    suspend fun getRequestDraft(
        accNo: String,
        owner: String,
    ): SubmissionRequest = requestService.getEditableRequest(accNo, owner)

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
        require(requestService.hasProcessingRequest(accNo).not()) { throw ConcurrentRequestDraftException(accNo) }

        val requestDraft = getOrCreateRequestDraftFromSubmission(accNo, owner)
        val modificationTime = Instant.now()

        requestService.updateRequestDraft(requestDraft.accNo, owner, draft, modificationTime)
        logger.info { "$accNo $owner Draft with key '$accNo' UPDATED for user '$owner'" }

        return requestDraft.copy(draft = draft, modificationTime = modificationTime.atOffset(UTC))
    }

    suspend fun createRequestDraft(
        draft: String,
        user: String,
        attachTo: String?,
    ): SubmissionRequest {
        val accNo = accNoService.calculateAccNo(attachTo, user)
        return saveRequest(draft, user, accNo.toString(), true)
    }

    public suspend fun createActiveRequestByAccNo(
        draft: String,
        owner: String,
        accNo: String,
        attachTo: String?,
    ): SubmissionRequest {
        accNoService.checkAccess(accNo, owner, attachTo)
        return saveRequest(draft, owner, accNo, false)
    }

    private suspend fun saveRequest(
        draft: String,
        owner: String,
        accNo: String,
        newSubmission: Boolean,
    ): SubmissionRequest {
        val creationTime = Instant.now()
        val request =
            SubmissionRequest(
                accNo = accNo,
                version = submissionPersistenceService.getNextVersion(accNo),
                owner = owner,
                newSubmission = newSubmission,
                draft = draft,
                status = DRAFT,
                modificationTime = creationTime.atOffset(UTC),
            )
        requestService.saveRequest(request)
        return request
    }

    private suspend fun createRequestDraftFromSubmission(
        accNo: String,
        owner: String,
    ): SubmissionRequest {
        require(userPrivilegesService.canResubmit(owner, accNo)) { throw UserCanNotUpdateSubmit(accNo, owner) }
        require(requestService.hasProcessingRequest(accNo).not()) { throw ConcurrentRequestDraftException(accNo) }

        val current = submissionQueryService.getExtByAccNo(accNo)
        val submission = toSubmissionMapper.toSimpleSubmission(current)
        val draft = serializationService.serializeSubmission(submission, JsonPretty)
        return createActiveRequestByAccNo(draft, owner, accNo, submission.attachTo)
    }
}
