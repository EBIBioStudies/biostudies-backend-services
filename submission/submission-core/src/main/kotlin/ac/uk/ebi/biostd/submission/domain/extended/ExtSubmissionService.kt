package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.service.DoiService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.extensions.doi
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import ebi.ac.uk.util.date.isBeforeOrEqual
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.Instant
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList", "TooManyFunctions")
class ExtSubmissionService(
    private val doiService: DoiService,
    private val toSubmissionMapper: ToSubmissionMapper,
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val queryService: SubmissionPersistenceQueryService,
    private val privilegesService: IUserPrivilegesService,
    private val securityService: SecurityQueryService,
    private val eventsPublisherService: EventsPublisherService,
) {
    suspend fun reTriggerSubmission(
        accNo: String,
        version: Int,
    ): ExtSubmission = submissionSubmitter.handleRequest(accNo, version)

    suspend fun reTriggerSubmissionAsync(submissions: List<SubmissionId>): Unit = submissionSubmitter.handleManyAsync(submissions)

    suspend fun refreshSubmission(
        user: String,
        accNo: String,
    ): Pair<String, Int> {
        logger.info { "$accNo $user Received async refresh request, accNo='$accNo'" }
        val submission = queryService.getExtByAccNo(accNo, true)
        val released = submission.releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()

        val toRefresh = submission.copy(released = released)
        val request =
            ExtSubmitRequest(
                notifyTo = user,
                submission = toRefresh,
            )
        val refreshed = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(refreshed.first, refreshed.second)
        return refreshed
    }

    suspend fun releaseSubmission(
        user: String,
        accNo: String,
        releaseDate: Instant,
    ): Pair<String, Int> {
        logger.info { "$accNo $user Received async release request, accNo='{$accNo}', releaseDate = $releaseDate" }
        val submission = queryService.getExtByAccNo(accNo, true)
        val newReleaseDate = releaseDate.asOffsetAtStartOfDay()
        val released = newReleaseDate.isBeforeOrEqual(OffsetDateTime.now()).orFalse()

        val toRelease = submission.copy(releaseTime = releaseDate.asOffsetAtStartOfDay(), released = released)
        val request =
            ExtSubmitRequest(
                notifyTo = user,
                submission = toRelease,
            )
        val releasedSub = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(releasedSub.first, releasedSub.second)
        return releasedSub
    }

    suspend fun generateDoi(
        user: String,
        accNo: String,
    ): SubmissionId {
        logger.info { "$accNo $user Received request to generate DOI" }
        val extSub = queryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        require(extSub.doi == null) { "DOI already exists for submission '$accNo'" }

        val sub = toSubmissionMapper.toSimpleSubmission(extSub).apply { doi = "true" }
        val doi = doiService.calculateDoi(extSub.accNo, sub, extSub)

        requireNotNull(doi) { "Failed to generate DOI for submission '$accNo'" }
        return submitExtAsync(user, extSub.copy(doi = doi))
    }

    suspend fun submitExt(
        user: String,
        sub: ExtSubmission,
    ): ExtSubmission {
        logger.info { "${sub.accNo} $user Received submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val request =
            ExtSubmitRequest(
                notifyTo = user,
                submission = submission,
            )
        val (accNo, version) = submissionSubmitter.createRqt(request)
        return submissionSubmitter.handleRequest(accNo, version)
    }

    suspend fun submitExtAsync(
        user: String,
        sub: ExtSubmission,
    ): SubmissionId {
        logger.info { "${sub.accNo} $user Received async submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val request =
            ExtSubmitRequest(
                notifyTo = user,
                submission = submission,
            )
        val (accNo, version) = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(accNo, version)
        return SubmissionId(accNo, version)
    }

    suspend fun transferSubmission(
        user: String,
        accNo: String,
        target: StorageMode,
    ) {
        logger.info { "$accNo $user Received transfer request with target='$target'" }
        val source = queryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        require(source.storageMode != target) { throw InvalidTransferTargetException() }

        val transfer = processSubmission(user, source.copy(storageMode = target))
        val request =
            ExtSubmitRequest(
                notifyTo = user,
                submission = transfer,
            )
        val (rqtAccNo, rqtVersion) = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(rqtAccNo, rqtVersion)
    }

    private suspend fun processSubmission(
        user: String,
        extSubmission: ExtSubmission,
    ): ExtSubmission {
        validateSubmission(extSubmission, user)
        return extSubmission.copy(submitter = user)
    }

    @Suppress("ThrowsCount")
    private suspend fun validateSubmission(
        sub: ExtSubmission,
        user: String,
    ) {
        if (privilegesService.canSubmitExtended(user).not()) throw UnauthorizedOperation(user)
        if (securityService.existsByEmail(sub.owner, false).not()) throw UserNotFoundException(sub.owner)

        if (sub.isCollection.not()) {
            sub.collections.forEach {
                if (queryService.existByAccNo(it.accNo).not()) throw CollectionNotFoundException(it.accNo)
            }
        }
    }
}

class InvalidTransferTargetException : RuntimeException("The target and current storage mode must be different")
