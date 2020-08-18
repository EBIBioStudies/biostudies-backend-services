package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.integration.FileMode
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.SubmissionRepository
import ac.uk.ebi.biostd.persistence.service.filesystem.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

open class SubmissionPersistenceService(
    private val subRepository: SubmissionRepository,
    private val subDataRepository: SubmissionDataRepository,
    private val userDataRepository: UserDataDataRepository,
    private val systemService: FileSystemService,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
) {

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    open fun processSubmission(submission: ExtSubmission, mode: FileMode): ExtSubmission {
        systemService.persistSubmissionFiles(submission, mode)

        val dbSubmission = subRepository.getDbSubmission(submission.accNo, submission.version)
        return toExtSubmissionMapper.toExtSubmission(processDbSubmission(dbSubmission))
    }

    private fun processDbSubmission(submission: DbSubmission): DbSubmission {
        subDataRepository.expireActiveVersions(submission.accNo)
        deleteSubmissionDrafts(submission.submitter.id, submission.accNo)
        deleteSubmissionDrafts(submission.owner.id, submission.accNo)
        subDataRepository.updateStatus(submission.accNo, submission.version, PROCESSED)

        return submission
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    open fun saveSubmission(dbSubmission: DbSubmission) {
        val nextVersion = (subDataRepository.getLastVersion(dbSubmission.accNo) ?: 0) + 1
        subDataRepository.save(dbSubmission.apply { status = PROCESSING; version = nextVersion })
    }

    private fun deleteSubmissionDrafts(userId: Long, accNo: String) =
        userDataRepository.deleteByUserIdAndKey(userId, accNo)
}
