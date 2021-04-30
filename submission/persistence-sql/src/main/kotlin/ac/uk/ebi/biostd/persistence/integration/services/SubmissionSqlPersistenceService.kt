package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.filesystem.FileSystemService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.mapping.extended.from.ToDbSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbSubmissionRequest
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRequestDataRepository
import ac.uk.ebi.biostd.persistence.repositories.UserDataDataRepository
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSING
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.math.absoluteValue

internal open class SubmissionSqlPersistenceService(
    private val subRepository: SubmissionQueryService,
    private val serializationService: ExtSerializationService,
    private val subDataRepository: SubmissionDataRepository,
    private val requestDataRepository: SubmissionRequestDataRepository,
    private val userDataRepository: UserDataDataRepository,
    private val systemService: FileSystemService,
    private val toDbMapper: ToDbSubmissionMapper
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    open fun saveSubmissionRequest(submission: ExtSubmission): ExtSubmission {
        val newVersion = submission.copy(
            version = getNextVersion(submission.accNo),
            status = ExtProcessingStatus.REQUESTED)
        subDataRepository.save(toDbMapper.toSubmissionDb(newVersion))
        requestDataRepository.save(asRequest(newVersion))
        return newVersion
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    open fun processSubmission(submission: ExtSubmission, mode: FileMode, draftKey: String?): ExtSubmission {
        subDataRepository.updateStatus(PROCESSING, submission.accNo, submission.version)
        systemService.persistSubmissionFiles(submission, mode)
        processDbSubmission(updateStored(submission), draftKey)
        return subRepository.getExtByAccNoAndVersion(submission.accNo, submission.version)
    }

    private fun updateStored(submission: ExtSubmission): ExtSubmission {
        val currentRecord = subDataRepository.getByAccNoAndVersion(submission.accNo, submission.version)
        subDataRepository.save(toDbMapper.toSubmissionDb(submission, currentRecord!!))
        return subRepository.getExtByAccNoAndVersion(submission.accNo, submission.version)
    }

    private fun asRequest(submission: ExtSubmission) =
        DbSubmissionRequest(submission.accNo, submission.version, serializationService.serialize(submission))

    private fun getNextVersion(accNo: String): Int {
        val lastVersion = subDataRepository.getLastVersion(accNo)?.version ?: 0
        return lastVersion.absoluteValue + 1
    }

    private fun processDbSubmission(submission: ExtSubmission, draftKey: String?): ExtSubmission {
        subDataRepository.expireActiveProcessedVersions(submission.accNo)
        deleteSubmissionDrafts(submission, draftKey)
        subDataRepository.updateStatus(PROCESSED, submission.accNo, submission.version)
        return submission
    }

    private fun deleteSubmissionDrafts(submission: ExtSubmission, draftKey: String?) {
        draftKey?.let { userDataRepository.deleteByKey(draftKey) }
        userDataRepository.deleteByUserEmailAndKey(submission.owner, submission.accNo)
        userDataRepository.deleteByUserEmailAndKey(submission.submitter, submission.accNo)
    }
}
