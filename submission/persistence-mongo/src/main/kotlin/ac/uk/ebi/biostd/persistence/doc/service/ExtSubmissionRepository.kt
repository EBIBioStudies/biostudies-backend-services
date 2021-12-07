package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission

typealias SubmissionDocData = Pair<DocSubmission, List<FileListDocFile>>

class ExtSubmissionRepository(
    private val subDataRepository: SubmissionDocDataRepository,
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
) {
    fun saveSubmission(submission: ExtSubmission, draftKey: String?): ExtSubmission {
        val docSubmission = save(submission.copy(status = ExtProcessingStatus.PROCESSING).toDocSubmission())
        updateCurrentRecords(docSubmission.accNo, docSubmission.owner, docSubmission.submitter, draftKey)
        subDataRepository.updateStatus(DocProcessingStatus.PROCESSED, docSubmission.accNo, docSubmission.version)
        return toExtSubmissionMapper.toExtSubmission(docSubmission)
    }

    private fun updateCurrentRecords(accNo: String, owner: String, submitter: String, draftKey: String?) {
        subDataRepository.expireActiveProcessedVersions(accNo)
        deleteSubmissionDrafts(accNo, owner, submitter, draftKey)
    }

    private fun deleteSubmissionDrafts(accNo: String, owner: String, submitter: String, draftKey: String?) {
        draftKey?.let { draftDocDataRepository.deleteByKey(draftKey) }
        draftDocDataRepository.deleteByUserIdAndKey(owner, accNo)
        draftDocDataRepository.deleteByUserIdAndKey(submitter, accNo)
    }

    private fun save(submission: SubmissionDocData): DocSubmission {
        val (docSubmission, files) = submission
        val savedSubmission = subDataRepository.save(docSubmission)
        files.forEach { fileListDocFileRepository.save(it) }
        return savedSubmission
    }
}
