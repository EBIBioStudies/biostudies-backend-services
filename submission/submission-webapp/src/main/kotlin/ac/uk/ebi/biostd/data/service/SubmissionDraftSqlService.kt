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
    override fun getSubmissionDraft(userId: Long, key: String): SubmissionDraft {
        val dbUser = userDataService.getUserData(userId, key) ?: create(userId, key)
        return SubmissionDraft(dbUser.key, dbUser.data)
    }

    @Transactional
    override fun updateSubmissionDraft(userId: Long, key: String, content: String): SubmissionDraft {
        val dbUser = userDataService.saveUserData(userId, key, content)
        return SubmissionDraft(dbUser.key, dbUser.data)
    }

    @Transactional
    override fun deleteSubmissionDraft(userId: Long, key: String): Unit = userDataService.delete(userId, key)

    @Transactional(readOnly = true)
    override fun getSubmissionsDraft(userId: Long, filter: PaginationFilter): List<SubmissionDraft> =
        userDataService.findAll(userId, filter).map { SubmissionDraft(it.key, it.data) }

    @Transactional
    override fun createSubmissionDraft(userId: Long, content: String): SubmissionDraft {
        val dbUser = userDataService.saveUserData(userId, "TMP_${Instant.now().toEpochMilli()}", content)
        return SubmissionDraft(dbUser.key, dbUser.data)
    }

    private fun create(userId: Long, key: String): DbUserData {
        val submission = submissionService.getSubmissionAsJson(key)
        return userDataService.saveUserData(userId, key, submission)
    }
}
