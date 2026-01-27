package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidMigrationTargetException
import ac.uk.ebi.biostd.submission.service.DoiService
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.mapping.to.ToSubmissionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.model.SubmissionTransferOptions
import ebi.ac.uk.model.extensions.doi
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import ebi.ac.uk.security.integration.exception.UserAlreadyRegister
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
    private val persistenceService: SubmissionPersistenceService,
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
    ): SubmissionId {
        logger.info { "$accNo $user Received async refresh request, accNo='$accNo'" }
        val submission = queryService.getExtByAccNo(accNo, true)
        val released = submission.releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()

        val toRefresh = submission.copy(released = released, version = persistenceService.getNextVersion(accNo))
        val request =
            ExtSubmitRequest(
                owner = user,
                newSubmission = false,
                submission = toRefresh,
            )
        val refreshed = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(refreshed.accNo, refreshed.version)
        return refreshed
    }

    suspend fun releaseSubmission(
        user: String,
        accNo: String,
        releaseDate: Instant,
    ): SubmissionId {
        logger.info { "$accNo $user Received async release request, accNo='{$accNo}', releaseDate = $releaseDate" }
        val submission = queryService.getExtByAccNo(accNo, true)
        val newReleaseDate = releaseDate.asOffsetAtStartOfDay()
        val released = newReleaseDate.isBeforeOrEqual(OffsetDateTime.now()).orFalse()

        val toRelease =
            submission.copy(
                releaseTime = releaseDate.asOffsetAtStartOfDay(),
                released = released,
                version = persistenceService.getNextVersion(accNo),
            )
        val request =
            ExtSubmitRequest(
                owner = user,
                newSubmission = false,
                submission = toRelease,
            )
        val releasedSub = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(releasedSub.accNo, releasedSub.version)
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
        return submitExtAsync(user, extSub.copy(doi = doi, version = persistenceService.getNextVersion(accNo)))
    }

    suspend fun submitExt(
        user: String,
        sub: ExtSubmission,
    ): ExtSubmission {
        logger.info { "${sub.accNo} $user Received submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val request =
            ExtSubmitRequest(
                owner = user,
                newSubmission = queryService.existByAccNo(sub.accNo),
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
                owner = user,
                newSubmission = queryService.existByAccNo(sub.accNo),
                submission = submission,
            )
        val (accNo, version) = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(accNo, version)
        return SubmissionId(accNo, version)
    }

    suspend fun migrateSubmission(
        user: String,
        accNo: String,
        target: StorageMode,
    ): SubmissionId {
        logger.info { "$accNo $user Received migration request with target='$target'" }
        val source = queryService.getExtByAccNo(accNo, includeFileListFiles = true, includeLinkListLinks = true)
        require(source.storageMode != target) { throw InvalidMigrationTargetException() }

        val newVersion = source.copy(storageMode = target, version = persistenceService.getNextVersion(accNo))
        val toMigrate = processSubmission(user, newVersion)

        val request =
            ExtSubmitRequest(
                owner = user,
                newSubmission = false,
                submission = toMigrate,
            )
        val submissionId = submissionSubmitter.createRqt(request)
        eventsPublisherService.submissionRequest(submissionId.accNo, submissionId.version)
        return submissionId
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

    suspend fun transferEmailUpdate(
        user: String,
        options: SubmissionTransferOptions,
    ) {
        require(securityService.existsByEmail(options.newOwner, onlyActive = false).not()) {
            throw UserAlreadyRegister(options.newOwner)
        }

        val owner = securityService.getUser(options.owner)
        transferSubmissions(user, options.copy(userName = owner.fullName))
    }

    suspend fun transferSubmissions(
        user: String,
        options: SubmissionTransferOptions,
    ) {
        val userName = options.userName.orEmpty()
        val (owner, newOwner, _, accNoList) = options

        fun validateUsers() {
            require(securityService.existsByEmail(owner, onlyActive = false)) { throw UserNotFoundException(owner) }
            if (securityService.existsByEmail(newOwner, onlyActive = false).not()) {
                require(userName.isNotBlank()) { "User name required for new owner" }
                securityService.getOrCreateInactive(newOwner, userName)
            }
        }

        suspend fun transfer(accNo: String) {
            logger.info { "Transferring submission $accNo from $owner to $newOwner" }
            require(privilegesService.canTransferSubmission(user, accNo)) { throw UnauthorizedOperation(user) }
            persistenceService.setOwner(accNo, newOwner)
        }

        validateUsers()
        queryService
            .getSubmissionsByOwner(owner, accNoList)
            .collect { transfer(it.accNo) }
    }
}
