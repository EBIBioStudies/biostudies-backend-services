package ac.uk.ebi.biostd.data.service

import ac.uk.ebi.biostd.persistence.common.request.PaginationFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.model.DbUserData
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ebi.ac.uk.model.SubmissionDraft
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

open class SubmissionDraftSqlService(
    private val userDataService: UserDataService,
    private val submissionService: SubmissionService
) : SubmissionDraftService {

    @Transactional(readOnly = true)
    override fun getSubmissionDraft(userEmail: String, key: String): SubmissionDraft {
        val dbUser = userDataService.getUserData(userEmail, key) ?: create(userEmail, key)
        return SubmissionDraft(dbUser.key, dbUser.data)
    }

    @Transactional
    override fun updateSubmissionDraft(userEmail: String, key: String, content: String): SubmissionDraft {
        val dbUser = userDataService.saveUserData(userEmail, key, content)
        return SubmissionDraft(dbUser.key, dbUser.data)
    }

    @Transactional
    override fun deleteSubmissionDraft(userEmail: String, key: String): Unit = userDataService.delete(userEmail, key)

    @Transactional(readOnly = true)
    override fun getSubmissionsDraft(userEmail: String, filter: PaginationFilter): List<SubmissionDraft> =
        userDataService.findAll(userEmail, filter).map { SubmissionDraft(it.key, it.data) }

    @Transactional
    override fun createSubmissionDraft(userEmail: String, content: String): SubmissionDraft {
        val dbUser = userDataService.saveUserData(userEmail, "TMP_${Instant.now().toEpochMilli()}", content)
        return SubmissionDraft(dbUser.key, dbUser.data)
    }

    private fun create(user: String, key: String): DbUserData {
        val submission = submissionService.getSubmissionAsJson(key)
        return userDataService.saveUserData(user, key, submission)
    }
}
