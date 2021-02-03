package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.request.SaveSubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestService
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}

class ExtSubmissionService(
    private val persistenceService: SubmissionRequestService,
    private val submissionRepository: SubmissionQueryService,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getExtendedSubmission(accNo: String): ExtSubmission = submissionRepository.getExtByAccNo(accNo)

    fun submitExtendedSubmission(user: String, extSubmission: ExtSubmission): ExtSubmission {
        validateUser(user)
        return persistenceService.saveAndProcessSubmissionRequest(SaveSubmissionRequest(extSubmission, COPY))
    }

    fun getExtendedSubmissions(request: ExtPageRequest): Page<ExtSubmission> {
        val filter = SubmissionFilter(
            rTimeFrom = OffsetDateTime.parse(request.fromRTime),
            rTimeTo = OffsetDateTime.parse(request.toRTime),
            released = request.released,
            limit = request.limit,
            offset = request.offset
        )

        val page = submissionRepository
            .getExtendedSubmissions(filter)
            .onEach { it.onFailure { logger.error { it.message ?: it.localizedMessage } } }
            .map { it.getOrNull() }
        val submissions = page.content.filterNotNull()

        return PageImpl(submissions, page.pageable, submissions.size.toLong())
    }

    private fun validateUser(user: String) = require(userPrivilegesService.canSubmitExtended(user)) {
        throw SecurityException("The user '$user' is not allowed to perform this action")
    }
}
