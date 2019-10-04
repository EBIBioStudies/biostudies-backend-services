package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.model.UserData
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import arrow.core.getOrElse

class DraftSubService(
    private val userDataService: UserDataService,
    private val submissionService: SubmissionService
) {

    fun getSubmission(userId: Long, accNo: String): UserData =
        userDataService.getUserData(userId, accNo).getOrElse { create(userId, accNo) }

    fun updateDraftSubmission(userId: Long, accNo: String, content: String): UserData =
        userDataService.saveUserData(userId, accNo, content)

    fun deleteDraftSubmission(userId: Long, accNo: String): Unit =
        userDataService.delete(userId, accNo)

    fun searchDraftSubmissions(userId: Long, searchText: String): List<UserData> =
        userDataService.searchByKey(userId, searchText)

    fun getDraftSubmissions(userId: Long): List<UserData> = userDataService.findAll(userId)

    private fun create(userId: Long, accNo: String): UserData {
        val submission = submissionService.getSubmissionAsJson(accNo)
        return userDataService.saveUserData(userId, accNo, submission)
    }
}
