package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.common.request.SubmissionRequest
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.service.ParentInfoService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class SubmissionSubmitter(
    private val submissionSubmitter: ExtSubmissionSubmitter,
    private val submissionProcessor: SubmissionProcessor,
    private val parentInfoService: ParentInfoService,
) {
    fun submit(rqt: SubmitRequest): ExtSubmission {
        val submission = process(rqt)
        val (accNo, version) = submissionSubmitter.submitAsync(SubmissionRequest(submission, rqt.draftKey))
        submissionSubmitter.processRequest(accNo, version)
        return submission
    }

    fun submitAsync(rqt: SubmitRequest): ExtSubmission {
        val submission = process(rqt)
        submissionSubmitter.submitAsync(SubmissionRequest(submission, rqt.draftKey))
        return submission
    }

    @Suppress("TooGenericExceptionCaught")
    private fun process(rqt: SubmitRequest): ExtSubmission {
        try {
            logger.info { "${rqt.accNo} ${rqt.owner} Processing submission request accNo='${rqt.accNo}'" }
            val submission = submissionProcessor.processSubmission(rqt)
            parentInfoService.executeCollectionValidators(submission)
            return submission
        } catch (exception: RuntimeException) {
            logger.error(exception) { "Error processing submission request accNo='${rqt.submission.accNo}'" }
            throw InvalidSubmissionException("Submission validation errors", listOf(exception))
        }
    }
}
