package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.submitter.SubmissionSubmitter
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.extended.model.StorageMode.NFS
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.exception.UnauthorizedOperation
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import uk.ac.ebi.events.service.EventsPublisherService
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions", "LongParameterList")
class ExtSubmissionService(
    private val submissionSubmitter: SubmissionSubmitter,
    private val submissionQueryService: SubmissionQueryService,
    private val privilegesService: IUserPrivilegesService,
    private val securityService: ISecurityQueryService,
    private val properties: ApplicationProperties,
    private val eventsPublisherService: EventsPublisherService
) {
    fun getExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission =
        submissionQueryService.getExtByAccNo(accNo, includeFileListFiles)

    fun findExtendedSubmission(accNo: String, includeFileListFiles: Boolean = false): ExtSubmission? =
        submissionQueryService.findExtByAccNo(accNo, includeFileListFiles)

    fun getReferencedFiles(accNo: String, fileListName: String): ExtFileTable =
        ExtFileTable(submissionQueryService.getReferencedFiles(accNo, fileListName))

    fun refreshSubmission(accNo: String, user: String): ExtSubmission {
        val submission = submissionQueryService.getExtByAccNo(accNo, includeFileListFiles = true)
        val (_, version) = submissionSubmitter.submitAsync(SubmissionRequest(submission.copy(submitter = user), COPY))
        val refreshedSubmission = submissionSubmitter.processRequest(accNo, version)
        eventsPublisherService.submissionsRefresh(refreshedSubmission.accNo, refreshedSubmission.owner)
        return refreshedSubmission
    }

    fun reTriggerSubmission(accNo: String, version: Int): ExtSubmission {
        return submissionSubmitter.processRequest(accNo, version)
    }

    fun submitExt(
        user: String,
        sub: ExtSubmission,
        fileMode: FileMode = COPY
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

    fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> {
        val filter = SubmissionFilter(
            rTimeFrom = request.fromRTime?.let { OffsetDateTime.parse(request.fromRTime) },
            rTimeTo = request.toRTime?.let { OffsetDateTime.parse(request.toRTime) },
            collection = request.collection,
            released = request.released,
            limit = request.limit,
            offset = request.offset
        )

        val page = submissionQueryService.getExtendedSubmissions(filter)
        return PageImpl(page.content, page.pageable, page.totalElements)
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
                if (submissionQueryService.existByAccNo(it.accNo).not()) throw CollectionNotFoundException(it.accNo)
            }
        }
    }
}
