package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

open class SubmissionDraftService(
    private val userDataService: UserDataService,
    private val submissionService: SubmissionService
) {

    @Transactional(readOnly = true)
    open fun getSubmissionDraft(userId: Long, key: String): DbUserData =
        userDataService.getUserData(userId, key) ?: create(userId, key)

    @Transactional
    open fun updateSubmissionDraft(userId: Long, key: String, content: String): DbUserData =
        userDataService.saveUserData(userId, key, content)

    @Transactional
    open fun deleteSubmissionDraft(userId: Long, key: String) = userDataService.delete(userId, key)

    @Transactional(readOnly = true)
    open fun getSubmissionsDraft(userId: Long, filter: PaginationFilter = PaginationFilter()): List<DbUserData> =
        userDataService.findAll(userId, filter)

    @Transactional
    open fun createSubmissionDraft(userId: Long, content: String): DbUserData {
        return userDataService.saveUserData(userId, "TMP_${Instant.now().toEpochMilli()}", content)
    }

    private fun create(userId: Long, key: String): DbUserData {
        val submission = submissionService.getSubmissionAsJson(key)
        return userDataService.saveUserData(userId, key, submission)
    }
}
