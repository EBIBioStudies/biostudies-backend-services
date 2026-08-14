package ac.uk.ebi.biostd.submission.domain.extended

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation.EMAIL_UPDATE
import ac.uk.ebi.biostd.persistence.common.model.TransferOperation.TRANSFER
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.TransferLogDataService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.persistence.repositories.UserDataRepository
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidMigrationTargetException
import ac.uk.ebi.biostd.submission.service.DoiService
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
import ebi.ac.uk.security.integration.exception.UserAlreadyRegisteredException
import ebi.ac.uk.util.date.asOffsetAtStartOfDay
import ebi.ac.uk.util.date.isBeforeOrEqual
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.time.Duration

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
    private val requestService: SubmissionRequestPersistenceService,
    private val userRepository: UserDataRepository,
    private val transferLogDataService: TransferLogDataService,
) {
    suspend fun reTriggerSubmission(
        accNo: String,
        version: Int,
    ): ExtSubmission = submissionSubmitter.handleRequest(accNo, version)

    suspend fun reTriggerSubmissionAsync(submissions: List<SubmissionId>): Unit = submissionSubmitter.handleManyAsync(submissions)

    suspend fun reindexSubmission(accNo: String) {
        val sub = queryService.getExtByAccNo(accNo, includeFileListFiles = false, includeLinkListLinks = false)
        eventsPublisherService.submissionsRefresh(sub.accNo, sub.owner)
    }

    suspend fun refreshSubmission(
        user: String,
        accNo: String,
    ): SubmissionId {
        logger.info { "$accNo $user Received async refresh request, accNo='$accNo'" }
        val submission = queryService.getExtByAccNo(accNo, true)
        val released = submission.releaseTime.isBeforeOrEqual(OffsetDateTime.now())

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
        val released = newReleaseDate.isBeforeOrEqual(OffsetDateTime.now())

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
        val submission = processSubmission(user, sub).copy(version = persistenceService.getNextVersion(sub.accNo))
        val request =
            ExtSubmitRequest(
                owner = user,
                newSubmission = queryService.existByAccNo(sub.accNo),
                submission = submission,
            )
        val (accNo, version) = submissionSubmitter.createRqt(request)
        return submissionSubmitter.handleRequest(accNo, version)
    }

    suspend fun submitExt(
        user: String,
        sub: List<ExtSubmission>,
        waitTime: Duration,
    ): List<ExtSubmission> {
        val submissions =
            sub
                .map { processSubmission(user, it) }
                .map { it.copy(version = persistenceService.getNextVersion(it.accNo)) }
                .map {
                    ExtSubmitRequest(
                        owner = user,
                        newSubmission = queryService.existByAccNo(it.accNo),
                        submission = it,
                    )
                }.map { submissionSubmitter.createRqt(it) }
        val result = submissionSubmitter.handleMany(submissions, waitTime)
        logger.info { "Submitted ${result.size} submissions" }
        return result
    }

    suspend fun submitExtAsync(
        user: String,
        sub: ExtSubmission,
    ): SubmissionId {
        logger.info { "${sub.accNo} $user Received async submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub).copy(version = persistenceService.getNextVersion(sub.accNo))
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
        if (requestService.hasProcessingRequest(sub.accNo)) throw ConcurrentSubException(sub.accNo)
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
        val owner = options.owner.lowercase()
        val newOwner = options.newOwner.lowercase()

        fun validateUser() {
            require(owner != newOwner) { "The new e-mail can't be the same as the current one" }
            require(securityService.existsByEmail(newOwner, onlyActive = false).not()) {
                throw UserAlreadyRegisteredException(newOwner)
            }
        }

        fun updateUser() {
            val sourceUser = userRepository.findByEmail(owner) ?: throw UserNotFoundException(owner)
            sourceUser.email = newOwner
            userRepository.save(sourceUser)
        }

        validateUser()
        val submissions = queryService.getSubmissionsByOwner(owner).toList()
        checkCanTransfer(user, submissions.map { it.accNo })
        updateUser()
        requestService.transferDrafts(owner, newOwner)
        transferSubmissions(owner, newOwner, submissions)
        logTransfer(user, owner, newOwner, EMAIL_UPDATE)
    }

    suspend fun transferSubmissions(
        user: String,
        options: SubmissionTransferOptions,
    ) {
        val owner = options.owner.lowercase()
        val newOwner = options.newOwner.lowercase()
        val userName = options.userName.orEmpty()

        fun validateUsers() {
            require(securityService.existsByEmail(owner, onlyActive = false)) { throw UserNotFoundException(owner) }
            if (securityService.existsByEmail(newOwner, onlyActive = false).not()) {
                require(userName.isNotBlank()) { "User name required for new owner" }
                securityService.getOrCreateInactive(newOwner, userName)
            }
        }

        validateUsers()
        val submissions = queryService.getSubmissionsByOwner(owner, options.accNoList).toList()
        checkCanTransfer(user, submissions.map { it.accNo })
        transferSubmissions(owner, newOwner, submissions)
        logTransfer(user, owner, newOwner, TRANSFER)
    }

    private suspend fun transferSubmissions(
        owner: String,
        newOwner: String,
        submissions: List<BasicSubmission>,
    ) = submissions.forEach {
        logger.info { "Transferring submission ${it.accNo} from $owner to $newOwner" }
        persistenceService.setOwner(it.accNo, newOwner)
        eventsPublisherService.submissionsRefresh(it.accNo, newOwner)
    }

    private suspend fun checkCanTransfer(
        user: String,
        accNos: List<String>,
    ) = require(accNos.all { privilegesService.canTransferSubmission(user, it) }) { throw UnauthorizedOperation(user) }

    private fun logTransfer(
        user: String,
        owner: String,
        newOwner: String,
        operation: TransferOperation,
    ) = transferLogDataService.logTransfer(user.lowercase(), owner, newOwner, operation)
}
