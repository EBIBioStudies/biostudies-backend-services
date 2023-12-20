package ac.uk.ebi.biostd.submission.domain.submitter

import ac.uk.ebi.biostd.common.properties.ApplicationProperties
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestCleaner
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestFinalizer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestIndexer
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestLoader
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestProcessor
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestReleaser
import ac.uk.ebi.biostd.submission.domain.request.SubmissionRequestSaver
import ebi.ac.uk.extended.model.ExtSubmission

@Suppress("LongParameterList", "TooManyFunctions")
class LocalExtSubmissionSubmitter(
    private val properties: ApplicationProperties,
    private val pageTabService: PageTabService,
    private val requestService: SubmissionRequestPersistenceService,
    private val persistenceService: SubmissionPersistenceService,
    private val requestIndexer: SubmissionRequestIndexer,
    private val requestLoader: SubmissionRequestLoader,
    private val requestProcessor: SubmissionRequestProcessor,
    private val requestReleaser: SubmissionRequestReleaser,
    private val requestCleaner: SubmissionRequestCleaner,
    private val requestSaver: SubmissionRequestSaver,
    private val requestFinalizer: SubmissionRequestFinalizer,
) : ExtSubmissionSubmitter {
    override suspend fun createRequest(rqt: ExtSubmitRequest): Pair<String, Int> {
        val withTabFiles = pageTabService.generatePageTab(rqt.submission)
        val submission = withTabFiles.copy(version = persistenceService.getNextVersion(rqt.submission.accNo))
        val request = SubmissionRequest(submission = submission, notifyTo = rqt.notifyTo, draftKey = rqt.draftKey)
        return requestService.createRequest(request)
    }

    override suspend fun indexRequest(accNo: String, version: Int) {
        requestIndexer.indexRequest(accNo, version, properties.processId)
    }

    override suspend fun loadRequest(accNo: String, version: Int) {
        requestLoader.loadRequest(accNo, version, properties.processId)
    }

    override suspend fun cleanRequest(accNo: String, version: Int) {
        requestCleaner.cleanCurrentVersion(accNo, version, properties.processId)
    }

    override suspend fun processRequest(accNo: String, version: Int) {
        requestProcessor.processRequest(accNo, version, properties.processId)
    }

    override suspend fun checkReleased(accNo: String, version: Int) {
        requestReleaser.checkReleased(accNo, version, properties.processId)
    }

    override suspend fun saveRequest(accNo: String, version: Int): ExtSubmission {
        return requestSaver.saveRequest(accNo, version, properties.processId)
    }

    override suspend fun finalizeRequest(accNo: String, version: Int): ExtSubmission {
        return requestFinalizer.finalizeRequest(accNo, version, properties.processId)
    }

    override suspend fun release(accNo: String) {
        requestReleaser.releaseSubmission(accNo)
    }

    override suspend fun handleRequest(accNo: String, version: Int): ExtSubmission {
        TODO("Not yet implemented")
    }
}
