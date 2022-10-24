package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ebi.ac.uk.extended.model.ExtSubmission
import kotlin.math.absoluteValue

internal class SubmissionMongoPersistenceService(
    private val submissionRepository: ExtSubmissionRepository,
    private val subDataRepository: SubmissionDocDataRepository,
) : SubmissionPersistenceService {
    override fun saveSubmission(submission: ExtSubmission): ExtSubmission {
        return submissionRepository.saveSubmission(submission)
    }

    override fun expirePreviousVersions(accNo: String) {
        submissionRepository.expirePreviousVersions(accNo)
    }

    override fun expireSubmissions(accNumbers: List<String>) {
        subDataRepository.expireVersions(accNumbers)
    }

    override fun setAsReleased(accNo: String) {
        subDataRepository.setAsReleased(accNo)
    }

    override fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getCurrentVersion(accNo) ?: 0
        return lastVersion.absoluteValue + 1
    }
}
