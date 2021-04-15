package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcMode
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace

private val logger = KotlinLogging.logger {}

class ErrorsDocService(
    private val errorsRepository: ErrorsRepository,
    private val subRepository: SubmissionRepository
) {
    suspend fun saveError(submission: SubmissionDoc, mode: PmcMode, throwable: Throwable) {
        logger.error { "Error ${asText(mode)} ${asText(submission)}, ${throwable.message}" }

        subRepository.update(submission.withStatus(getError(mode)))
        errorsRepository.save(SubmissionErrorDoc(submission, getStackTrace(throwable), mode))
    }

    private fun asText(submission: SubmissionDoc) =
        "submission accNo ='${submission.accno}', file '${submission.sourceFile}'"

    private fun getError(pmcMode: PmcMode) = when (pmcMode) {
        PmcMode.LOAD -> SubmissionStatus.ERROR_LOAD
        PmcMode.PROCESS -> SubmissionStatus.ERROR_PROCESS
        PmcMode.SUBMIT -> SubmissionStatus.ERROR_SUBMIT
    }

    private fun asText(pmcMode: PmcMode) = when (pmcMode) {
        PmcMode.LOAD -> "loading"
        PmcMode.PROCESS -> "processing"
        PmcMode.SUBMIT -> "submitting"
    }
    suspend fun saveError(sourceFile: String, submissionBody: String, process: PmcMode, throwable: Throwable) {
        logger.info { "Reporting error for submission in file $sourceFile" }
        errorsRepository.save(SubmissionErrorDoc(sourceFile, submissionBody, getStackTrace(throwable), process))
    }
}
