package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ac.uk.ebi.biostd.persistence.service.filesystem.FileSystemService
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.math.absoluteValue

internal open class SubmissionSqlPersistenceService(
    private val subRepository: SubmissionRepository,
    private val subDataRepository: SubmissionDataRepository,
    private val userDataRepository: UserDataDataRepository,
    private val systemService: FileSystemService,
    private val toDbMapper: ToDbSubmissionMapper
) : SubmissionPersistenceService {
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    override fun saveSubmissionRequest(submission: ExtSubmission): ExtSubmission {
        val newVersion = submission.copy(
            version = getNextVersion(submission.accNo),
            status = ExtProcessingStatus.REQUESTED)
        subDataRepository.save(toDbMapper.toSubmissionDb(newVersion))
        return newVersion
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    override fun processSubmission(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        subDataRepository.updateStatus(PROCESSING, submission.accNo, submission.version)
        systemService.persistSubmissionFiles(submission, mode)
        processDbSubmission(subRepository.getDbSubmission(submission.accNo, submission.version))
        return subRepository.getExtByAccNoAndVersion(submission.accNo, submission.version)
    }

    private fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getLastVersion(accNo)?.version ?: 0
        return lastVersion.absoluteValue + 1
    }

    private fun processDbSubmission(submission: DbSubmission): DbSubmission {
        subDataRepository.expireActiveVersions(submission.accNo)
        deleteSubmissionDrafts(submission.submitter.id, submission.accNo)
        deleteSubmissionDrafts(submission.owner.id, submission.accNo)
        subDataRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
        return submission
    }

    private fun deleteSubmissionDrafts(userId: Long, accNo: String) =
        userDataRepository.deleteByUserIdAndKey(userId, accNo)
}
