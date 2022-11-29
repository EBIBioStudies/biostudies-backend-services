package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ebi.ac.uk.extended.events.RequestCheckReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionSubmitter(
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val submissionProcessor: SubmissionProcessor,
    private val parentInfoService: ParentInfoService,
    private val draftService: SubmissionDraftPersistenceService,
) {
    fun submit(rqt: SubmitRequest): ExtSubmission {
        val submission = processRequest(rqt)
        val submitRequest = ExtSubmitRequest(submission, notifyTo = rqt.owner, rqt.draftKey)
        val (accNo, version) = submissionSubmitter.createRequest(submitRequest)
        submissionSubmitter.handleRequest(accNo, version)
        return submission
    }

    fun createRequest(rqt: SubmitRequest): ExtSubmission {
        val submission = processRequest(rqt)
        submissionSubmitter.createRequest(ExtSubmitRequest(submission, rqt.owner, rqt.draftKey))
        return submission
    }

    fun indexRequest(rqt: RequestCreated) {
        submissionSubmitter.indexRequest(rqt.accNo, rqt.version)
    }

    fun loadRequest(rqt: RequestIndexed) {
        return submissionSubmitter.loadRequest(rqt.accNo, rqt.version)
    }

    fun cleanRequest(rqt: RequestLoaded) {
        submissionSubmitter.cleanRequest(rqt.accNo, rqt.version)
    }

    fun processRequest(rqt: RequestCleaned) {
        submissionSubmitter.processRequest(rqt.accNo, rqt.version)
    }

    fun checkReleased(rqt: RequestFilesCopied) {
        submissionSubmitter.checkReleased(rqt.accNo, rqt.version)
    }

    fun saveRequest(rqt: RequestCheckReleased): ExtSubmission {
        return submissionSubmitter.saveRequest(rqt.accNo, rqt.version)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun processRequest(rqt: SubmitRequest): ExtSubmission {
        try {
            logger.info { "${rqt.accNo} ${rqt.owner} Started processing submission request" }

            rqt.draftKey?.let { draftService.setProcessingStatus(rqt.owner, it) }
            val processed = submissionProcessor.processSubmission(rqt)
            parentInfoService.executeCollectionValidators(processed)
            rqt.draftKey?.let { draftService.setAcceptedStatus(it) }
            logger.info { "${rqt.accNo} ${rqt.owner} Finished processing submission request" }

            return processed
        } catch (exception: RuntimeException) {
            logger.error(exception) { "${rqt.accNo} ${rqt.owner} Error processing submission request" }
            rqt.draftKey?.let { draftService.setActiveStatus(it) }
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }
}
