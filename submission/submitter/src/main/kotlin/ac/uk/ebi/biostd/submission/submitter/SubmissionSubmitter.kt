package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ebi.ac.uk.extended.events.RequestCleaned
import ebi.ac.uk.extended.events.RequestCreated
import ebi.ac.uk.extended.events.RequestLoaded
import ebi.ac.uk.extended.events.RequestProcessed
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
        val (accNo, version) = submissionSubmitter.createRequest(ExtSubmitRequest(submission, rqt.draftKey))
        submissionSubmitter.handleRequest(accNo, version)
        return submission
    }

    fun createRequest(rqt: SubmitRequest): ExtSubmission {
        val submission = processRequest(rqt)
        submissionSubmitter.createRequest(ExtSubmitRequest(submission, rqt.draftKey))
        return submission
    }

    fun loadRequest(rqt: RequestCreated): ExtSubmission {
        return submissionSubmitter.loadRequest(rqt.accNo, rqt.version)
    }

    fun cleanRequest(rqt: RequestLoaded) {
        submissionSubmitter.cleanRequest(rqt.accNo, rqt.version)
    }

    fun processRequest(rqt: RequestCleaned): ExtSubmission {
        return submissionSubmitter.processRequest(rqt.accNo, rqt.version)
    }

    fun checkReleased(rqt: RequestProcessed): ExtSubmission {
        return submissionSubmitter.checkReleased(rqt.accNo, rqt.version)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun processRequest(rqt: SubmitRequest): ExtSubmission {
        try {
            logger.info { "${rqt.accNo} ${rqt.owner} Started processing submission request" }

            rqt.draftKey?.let { draftService.setProcessingStatus(rqt.owner, it) }
            val submission = submissionProcessor.processSubmission(rqt)
            parentInfoService.executeCollectionValidators(submission)
            rqt.draftKey?.let { draftService.setDeleteStatus(it) }
            logger.info { "${rqt.accNo} ${rqt.owner} Finished processing submission request" }

            return submission
        } catch (exception: RuntimeException) {
            logger.error(exception) { "${rqt.accNo} ${rqt.owner} Error processing submission request" }
            rqt.draftKey?.let { draftService.setActiveStatus(it) }
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }
}
