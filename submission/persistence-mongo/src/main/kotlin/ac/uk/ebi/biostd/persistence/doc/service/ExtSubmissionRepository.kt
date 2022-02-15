package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDraftDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.mapping.from.toDocSubmission
import ac.uk.ebi.biostd.persistence.doc.mapping.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
private const val FILES_CHUNK_SIZE = 100

class ExtSubmissionRepository(
    private val subDataRepository: SubmissionDocDataRepository,
    private val draftDocDataRepository: SubmissionDraftDocDataRepository,
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val toExtSubmissionMapper: ToExtSubmissionMapper
) {
    fun saveSubmission(submission: ExtSubmission, draftKey: String?): ExtSubmission {
        val docSubmission = save(submission.copy(status = ExtProcessingStatus.PROCESSING))
        updateCurrentRecords(docSubmission.accNo, docSubmission.owner, docSubmission.submitter, draftKey)
        subDataRepository.updateStatus(DocProcessingStatus.PROCESSED, docSubmission.accNo, docSubmission.version)
        return toExtSubmissionMapper.toExtSubmission(docSubmission, false)
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

    private fun save(submission: ExtSubmission): DocSubmission {
        logger.info { "mapping submission ${submission.accNo} into doc submission" }
        val (docSubmission, files) = submission.toDocSubmission()
        logger.info { "mapped submission ${submission.accNo}" }

        logger.info { "saving submission ${docSubmission.accNo}" }
        val savedSubmission = subDataRepository.save(docSubmission)
        logger.info { "saved submission ${docSubmission.accNo}" }

        logger.info { "saving ${files.count()} file list files ${docSubmission.accNo}" }
        files.chunked(FILES_CHUNK_SIZE).forEach { fileListDocFileRepository.saveAll(it) }
        logger.info { "saved ${files.count()} file list files ${docSubmission.accNo}" }
        return savedSubmission
    }
}
