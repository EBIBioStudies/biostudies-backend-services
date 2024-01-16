package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionDraftPersistenceService
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class SubmissionSubmitter(
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val submissionProcessor: SubmissionProcessor,
    private val collectionValidationService: CollectionValidationService,
    private val draftService: SubmissionDraftPersistenceService,
) {
    suspend fun createRequest(rqt: SubmitRequest): ExtSubmission {
        val submission = processRequest(rqt)
        submissionSubmitter.createRequest(ExtSubmitRequest(submission, rqt.owner, rqt.draftKey))
        return submission
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun processRequest(rqt: SubmitRequest): ExtSubmission {
        try {
            logger.info { "${rqt.accNo} ${rqt.owner} Started processing submission request" }
            rqt.draftKey?.let { startProcessingDraft(rqt.accNo, rqt.owner, it) }
            val processed = submissionProcessor.processSubmission(rqt)
            collectionValidationService.executeCollectionValidators(processed)
            rqt.draftKey?.let { acceptDraft(rqt.accNo, rqt.owner, it) }
            logger.info { "${rqt.accNo} ${rqt.owner} Finished processing submission request" }

            return processed
        } catch (exception: RuntimeException) {
            logger.error(exception) { "${rqt.accNo} ${rqt.owner} Error processing submission request" }
            rqt.draftKey?.let { reactivateDraft(rqt.accNo, rqt.owner, it) }
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }

    private suspend fun acceptDraft(accNo: String, owner: String, draftKey: String) {
        draftService.setAcceptedStatus(draftKey)
        logger.info { "$accNo $owner Status of draft with key '$draftKey' set to ACCEPTED" }
    }

    private suspend fun startProcessingDraft(accNo: String, owner: String, draftKey: String) {
        draftService.setProcessingStatus(owner, draftKey)
        logger.info { "$accNo $owner Status of draft with key '$draftKey' set to PROCESSING" }
    }

    private suspend fun reactivateDraft(accNo: String, owner: String, draftKey: String) {
        draftService.setActiveStatus(draftKey)
        logger.info { "$accNo $owner Status of draft with key '$draftKey' set to ACTIVE" }
    }
}
