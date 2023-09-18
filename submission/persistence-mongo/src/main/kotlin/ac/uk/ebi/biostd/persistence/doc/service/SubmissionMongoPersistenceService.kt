package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ebi.ac.uk.extended.model.ExtSubmission
import kotlin.math.absoluteValue

internal class SubmissionMongoPersistenceService(
    private val submissionRepository: ExtSubmissionRepository,
    private val subDataRepository: SubmissionDocDataRepository,
) : SubmissionPersistenceService {
    override suspend fun saveSubmission(submission: ExtSubmission): ExtSubmission {
        return submissionRepository.saveSubmission(submission)
    }

    override suspend fun expirePreviousVersions(accNo: String) {
        submissionRepository.expirePreviousVersions(accNo)
    }

    override suspend fun expireSubmissions(accNumbers: List<String>) {
        subDataRepository.expireVersions(accNumbers)
    }

    override suspend fun setAsReleased(accNo: String) {
        subDataRepository.setAsReleased(accNo)
    }

    override suspend fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentMaxVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }
}
