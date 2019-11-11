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
    fun getSubmissionDraft(userId: Long, accNo: String): UserData =
        userDataService.getUserData(userId, accNo) ?: create(userId, accNo)

    fun updateSubmissionDraft(userId: Long, accNo: String, content: String): UserData =
        userDataService.saveUserData(userId, accNo, content)

    fun deleteSubmissionDraft(userId: Long, accNo: String): Unit =
        userDataService.delete(userId, accNo)

    fun getSubmissionsDraft(userId: Long, filter: PaginationFilter = PaginationFilter()): List<UserData> =
        userDataService.findAll(userId, filter)

    fun createSubmissionDraft(userId: Long, content: String): UserData {
        return userDataService.saveUserData(userId, "TMP_${Instant.now().toEpochMilli()}", content)
    }

    private fun create(userId: Long, accNo: String): UserData {
        val submission = submissionService.getSubmissionAsJson(accNo)
        return userDataService.saveUserData(userId, accNo, submission)
    }
}
