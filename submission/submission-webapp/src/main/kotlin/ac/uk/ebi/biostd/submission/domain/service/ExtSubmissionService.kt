package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.submission.submitter.ExtSubmissionSubmitter
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
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
    private val queryService: SubmissionPersistenceQueryService,
    private val privilegesService: IUserPrivilegesService,
    private val securityService: ISecurityQueryService,
    private val properties: ApplicationProperties,
    private val eventsPublisherService: EventsPublisherService,
    private val fileStorageService: FileStorageService,
) {
    fun reTriggerSubmission(accNo: String, version: Int): ExtSubmission {
        return submissionSubmitter.handleRequest(accNo, version)
    }

    fun submitExt(
        user: String,
        sub: ExtSubmission,
    ): ExtSubmission {
        logger.info { "${sub.accNo} $user Received submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val (accNo, version) = submissionSubmitter.createRequest(ExtSubmitRequest(submission))
        return submissionSubmitter.handleRequest(accNo, version)
    }

    fun submitExtAsync(
        user: String,
        sub: ExtSubmission,
    ) {
        logger.info { "${sub.accNo} $user Received async submit request for ext submission ${sub.accNo}" }
        val submission = processSubmission(user, sub)
        val (accNo, version) = submissionSubmitter.createRequest(ExtSubmitRequest(submission))
        eventsPublisherService.submissionRequest(accNo, version)
    }

    private fun processSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateSubmission(extSubmission, user)
        return extSubmission.copy(
            submitter = user,
            modificationTime = OffsetDateTime.now(),
            storageMode = if (properties.persistence.enableFire) FIRE else NFS
        )
    }

    @Suppress("ThrowsCount")
    private fun validateSubmission(sub: ExtSubmission, user: String) {
        if (privilegesService.canSubmitExtended(user).not()) throw UnauthorizedOperation(user)
        if (securityService.existsByEmail(sub.owner, false).not()) throw UserNotFoundException(sub.owner)

        if (sub.isCollection.not()) {
            sub.collections.forEach {
                if (queryService.existByAccNo(it.accNo).not()) throw CollectionNotFoundException(it.accNo)
            }
        }
    }
}
