package ac.uk.ebi.biostd.submission.domain.submission

import ac.uk.ebi.biostd.persistence.common.exception.ConcurrentSubException
import ac.uk.ebi.biostd.persistence.common.request.ExtSubmitRequest
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ac.uk.ebi.biostd.submission.domain.submitter.ExtSubmissionSubmitter
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.validator.collection.CollectionValidationService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.DRAFT
import ebi.ac.uk.model.RequestStatus.SUBMITTED
import ebi.ac.uk.model.SubmissionId
import mu.KotlinLogging
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
class SubmissionSubmitter(
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val submissionProcessor: SubmissionProcessor,
    private val collectionValidationService: CollectionValidationService,
    private val requestService: SubmissionRequestPersistenceService,
) {
    suspend fun processRequestDraft(rqt: SubmitRequest): ExtSubmission {
        val sub = processRequest(rqt)
        val extRqt = ExtSubmitRequest(sub.owner, rqt.owner, sub, rqt.silentMode, rqt.singleJobMode)

        checkProcessingRequests(sub.accNo, sub.version)
        requestService.setSubRequestAccNo(rqt.accNo, sub.accNo, rqt.owner, Instant.now())
        submissionSubmitter.createRqt(extRqt)

        return sub
    }

    suspend fun handleRequest(
        accNo: String,
        version: Int,
    ): ExtSubmission = submissionSubmitter.handleRequest(accNo, version)

    suspend fun handleRequestAsync(
        accNo: String,
        version: Int,
    ): Unit = submissionSubmitter.handleRequestAsync(accNo, version)

    suspend fun handleManyAsync(submissions: List<SubmissionId>) {
        submissionSubmitter.handleManyAsync(submissions)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun processRequest(rqt: SubmitRequest): ExtSubmission {
        try {
            logger.info { "${rqt.accNo} ${rqt.owner} Started processing submission request" }
            startProcessingDraft(rqt.accNo, rqt.owner)
            val processed = submissionProcessor.processSubmission(rqt)
            collectionValidationService.executeCollectionValidators(processed)
            logger.info { "${rqt.accNo} ${rqt.owner} Finished processing submission request" }

            return processed
        } catch (exception: RuntimeException) {
            logger.error(exception) { "${rqt.accNo} ${rqt.owner} Error processing submission request" }
            reactivateDraft(rqt.accNo, rqt.owner)
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }

    private suspend fun checkProcessingRequests(
        accNo: String,
        version: Int,
    ) = require(requestService.hasActiveRequest(accNo).not()) { throw ConcurrentSubException(accNo, version) }

    private suspend fun startProcessingDraft(
        accNo: String,
        owner: String,
    ) {
        requestService.setDraftStatus(accNo, owner, SUBMITTED, Instant.now())
        logger.info { "$accNo $owner Status of request draft with key '$accNo' set to PROCESSING" }
    }

    private suspend fun reactivateDraft(
        accNo: String,
        owner: String,
    ) {
        requestService.setDraftStatus(accNo, owner, DRAFT, Instant.now())
        logger.info { "$accNo $owner Status of request draft with key '$accNo' set to ACTIVE" }
    }
}
