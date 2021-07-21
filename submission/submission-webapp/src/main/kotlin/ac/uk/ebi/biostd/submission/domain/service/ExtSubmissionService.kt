package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.exception.UserNotFoundException
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.isCollection
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

class ExtSubmissionService(
    private val requestService: SubmissionRequestService,
    private val submissionRepository: SubmissionQueryService,
    private val userPrivilegesService: IUserPrivilegesService,
    private val securityQueryService: ISecurityQueryService
) {
    fun getExtendedSubmission(accNo: String): ExtSubmission = submissionRepository.getExtByAccNo(accNo)

    fun getReferencedFiles(
        accNo: String,
        fileListName: String
    ): ExtFileTable = ExtFileTable(submissionRepository.getReferencedFiles(accNo, fileListName))

    fun submitExtendedSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateSubmitter(user)
        validateSubmission(extSubmission)
        val submission = extSubmission.copy(submitter = user)

        return requestService.saveAndProcessSubmissionRequest(SaveSubmissionRequest(submission, COPY))
    }

    fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> {
        val filter = SubmissionFilter(
            rTimeFrom = request.fromRTime?.let { OffsetDateTime.parse(request.fromRTime) },
            rTimeTo = request.toRTime?.let { OffsetDateTime.parse(request.toRTime) },
            released = request.released,
            limit = request.limit,
            offset = request.offset
        )

        val page = submissionRepository
            .getExtendedSubmissions(filter)
            .onEach { it.onFailure { logger.error { it.message ?: it.localizedMessage } } }
            .map { it.getOrNull() }
        val submissions = page.content.filterNotNull()

        return PageImpl(submissions, page.pageable, page.totalElements)
    }

    private fun validateSubmission(submission: ExtSubmission) {
        validateOwner(submission.owner)

        if (submission.isCollection.not()) {
            submission.collections.forEach { validateCollection(it.accNo) }
        }
    }

    private fun validateSubmitter(user: String) = require(userPrivilegesService.canSubmitExtended(user)) {
        throw SecurityException("The user '$user' is not allowed to perform this action")
    }

    private fun validateOwner(email: String) = require(securityQueryService.existsByEmail(email)) {
        throw UserNotFoundException(email)
    }

    private fun validateCollection(accNo: String) = require(submissionRepository.existByAccNo(accNo)) {
        throw CollectionNotFoundException(accNo)
    }
}
