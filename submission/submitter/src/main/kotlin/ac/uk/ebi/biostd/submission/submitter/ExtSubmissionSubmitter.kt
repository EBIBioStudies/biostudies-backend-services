package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size

class ExtSubmissionSubmitter(
    private val persistenceService: SubmissionPersistenceService,
    private val draftService: SubmissionDraftService,
    private val requestProcessor: RequestProcessor,
) {
    fun submitAsync(request: SubmissionRequest): Pair<String, Int> {
        return saveRequest(request, request.submission.submitter)
    }

    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val sub = requestProcessor.loadRequest(accNo, version)
        return persistenceService.processSubmissionRequest(sub)
    }

    fun release(request: ReleaseRequest) {
        val (accNo, owner, relPath) = request
        persistenceService.releaseSubmission(accNo, owner, relPath)
    }

    private fun saveRequest(request: SubmissionRequest, owner: String): Pair<String, Int> {
        val saved = persistenceService.saveSubmissionRequest(request)
        request.draftKey?.let { draftService.setProcessingStatus(owner, it) }
        return saved
    }
}

class RequestProcessor(
    private val submissionPersistenceQueryService: SubmissionPersistenceQueryService,
    private val fileProcessingService: FileProcessingService,
) {

    internal fun loadRequest(accNo: String, version: Int): SubmissionRequest {
        val rqt = submissionPersistenceQueryService.getPendingRequest(accNo, version)
        val full = fileProcessingService.processFiles(rqt.submission) { loadFileAttributes(it) }
        return SubmissionRequest(full, rqt.fileMode, rqt.draftKey)
    }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
