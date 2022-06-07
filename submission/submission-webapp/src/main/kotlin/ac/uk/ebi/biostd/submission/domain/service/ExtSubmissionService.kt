package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Suppress("LongParameterList")
class ExtSubmissionService(
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val persistenceService: SubmissionPersistenceService,
    private val privilegesService: IUserPrivilegesService,
    private val securityService: ISecurityQueryService,
    private val properties: ApplicationProperties,
    private val eventsPublisherService: EventsPublisherService
) {
    fun refreshSubmission(accNo: String, user: String): ExtSubmission {
        val sub = submissionPersistenceQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        val response = submitExt(user, sub, FileMode.COPY)
        eventsPublisherService.submissionsRefresh(sub.accNo, sub.owner)
        return response
    }

    fun reTriggerSubmission(accNo: String, version: Int): ExtSubmission {
        return submissionSubmitter.processRequest(accNo, version)
    }

    fun submitExt(
        user: String,
        sub: ExtSubmission,
        fileMode: FileMode = FileMode.COPY
    ): ExtSubmission {
        logger.info { "${sub.accNo} $user Received submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val (accNo, version) = submissionSubmitter.submitAsync(SubmissionRequest(submission, fileMode))
        return submissionSubmitter.processRequest(accNo, version)
    }

    fun submitExtAsync(
        user: String,
        sub: ExtSubmission,
        fileMode: FileMode
    ) {
        logger.info { "${sub.accNo} $user Received async submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val (accNo, version) = submissionSubmitter.submitAsync(SubmissionRequest(submission, fileMode))
        eventsPublisherService.submissionRequest(accNo, version)
    }

    private fun processSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateSubmission(extSubmission, user)
        return extSubmission.copy(
            submitter = user,
            version = persistenceService.getNextVersion(extSubmission.accNo),
            modificationTime = OffsetDateTime.now(),
            storageMode = if (properties.persistence.enableFire) StorageMode.FIRE else StorageMode.NFS
        )
    }

    @Suppress("ThrowsCount")
    private fun validateSubmission(sub: ExtSubmission, user: String) {
        if (privilegesService.canSubmitExtended(user).not()) throw UnauthorizedOperation(user)
        if (securityService.existsByEmail(sub.owner, false).not()) throw UserNotFoundException(sub.owner)

        if (sub.isCollection.not()) {
            sub.collections.forEach {
                if (submissionPersistenceQueryService.existByAccNo(it.accNo)
                    .not()
                ) throw CollectionNotFoundException(it.accNo)
            }
        }
    }
}
