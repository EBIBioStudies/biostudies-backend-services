package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.filter.PaginationFilter
import ac.uk.ebi.biostd.persistence.model.UserData
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import arrow.core.getOrElse
import java.time.Instant

class SubmissionDraftService(
    private val userDataService: UserDataService,
    private val submissionService: SubmissionService
) {
    fun getSubmissionDraft(userId: Long, key: String): UserData =
        userDataService.getUserData(userId, key).getOrElse { create(userId, key) }

    fun updateSubmissionDraft(userId: Long, key: String, content: String): UserData =
        userDataService.saveUserData(userId, key, content)

    fun deleteSubmissionDraft(userId: Long, key: String) = userDataService.delete(userId, key)

    fun getSubmissionsDraft(userId: Long, filter: PaginationFilter = PaginationFilter()): List<UserData> =
        userDataService.findAll(userId, filter)

    fun createSubmissionDraft(userId: Long, content: String): UserData {
        return userDataService.saveUserData(userId, "TMP_${Instant.now().toEpochMilli()}", content)
    }

    private fun create(userId: Long, key: String): UserData {
        val submission = submissionService.getSubmissionAsJson(key)
        return userDataService.saveUserData(userId, key, submission)
    }
}
