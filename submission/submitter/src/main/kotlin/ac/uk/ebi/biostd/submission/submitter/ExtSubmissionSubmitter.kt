package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ac.uk.ebi.biostd.submission.model.ReleaseRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size

class ExtSubmissionSubmitter(
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val draftService: SubmissionDraftService,
    private val requestProcessor: RequestProcessor,
) {
    fun submitAsync(request: SubmissionRequest): Pair<String, Int> {
        return saveRequest(request, request.submission.submitter)
    }

    fun processRequest(accNo: String, version: Int): ExtSubmission {
        val sub = requestProcessor.loadRequest(accNo, version)
        return submissionPersistenceService.processSubmissionRequest(sub)
    }

    fun release(request: ReleaseRequest) {
        val (accNo, owner, relPath) = request
        submissionPersistenceService.releaseSubmission(accNo, owner, relPath)
    }

    private fun saveRequest(request: SubmissionRequest, owner: String): Pair<String, Int> {
        val saved = submissionPersistenceService.saveSubmissionRequest(request)
        request.draftKey?.let { draftService.setProcessingStatus(owner, it) }
        return saved
    }
}

class RequestProcessor(
    private val submissionPersistenceService: SubmissionPersistenceService,
    private val submissionQueryService: SubmissionQueryService,
    private val fileProcessingService: FileProcessingService,
) {

    internal fun loadRequest(accNo: String, version: Int): SubmissionRequest {
        val rqt = submissionQueryService.getPendingRequest(accNo, version)
        val full = fileProcessingService.processFiles(rqt.submission) { loadFileAttributes(it) }
        submissionPersistenceService.savePlainSubmissionRequest(SubmissionRequest(full, rqt.fileMode, rqt.draftKey))
        return submissionQueryService.getPendingRequest(accNo, version)
    }

    private fun loadFileAttributes(file: ExtFile): ExtFile = when (file) {
        is FireFile -> file
        is NfsFile -> file.copy(md5 = file.file.md5(), size = file.file.size())
    }
}
