package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ebi.ac.uk.extended.events.RequestCheckedReleased
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestFilesCopied
import ebi.ac.uk.extended.events.RequestIndexed
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestSubmissionProcessed
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
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

    fun saveRequest(rqt: RequestCheckedReleased): ExtSubmission {
        return submissionSubmitter.saveRequest(rqt.accNo, rqt.version)
    }

    fun postProcessRequest(rqt: RequestSubmissionProcessed) {
        submissionSubmitter.postProcessRequest(rqt.accNo, rqt.version)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun processRequest(rqt: SubmitRequest): ExtSubmission {
        try {
            logger.info { "${rqt.accNo} ${rqt.owner} Started processing submission request" }

            rqt.draftKey?.let { startProcessingDraft(rqt.accNo, rqt.owner, it) }
            val processed = submissionProcessor.processSubmission(rqt)
            parentInfoService.executeCollectionValidators(processed)
            rqt.draftKey?.let { acceptDraft(rqt.accNo, rqt.owner, it) }
            logger.info { "${rqt.accNo} ${rqt.owner} Finished processing submission request" }

            return processed
        } catch (exception: RuntimeException) {
            logger.error(exception) { "${rqt.accNo} ${rqt.owner} Error processing submission request" }
            rqt.draftKey?.let { reactivateDraft(rqt.accNo, rqt.owner, it) }
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }

    private fun acceptDraft(accNo: String, owner: String, draftKey: String) {
        draftService.setAcceptedStatus(draftKey)
        logger.info { "$accNo $owner Status of draft with key '$draftKey' set to ACCEPTED" }
    }

    private fun startProcessingDraft(accNo: String, owner: String, draftKey: String) {
        draftService.setProcessingStatus(owner, draftKey)
        logger.info { "$accNo $owner Status of draft with key '$draftKey' set to PROCESSING" }
    }

    private fun reactivateDraft(accNo: String, owner: String, draftKey: String) {
        draftService.setActiveStatus(draftKey)
        logger.info { "$accNo $owner Status of draft with key '$draftKey' set to ACTIVE" }
    }
}
