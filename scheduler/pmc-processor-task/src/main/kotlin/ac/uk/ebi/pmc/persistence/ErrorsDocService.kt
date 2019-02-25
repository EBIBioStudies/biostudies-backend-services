package ac.uk.ebi.pmc.persistence

import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionStatus
import ac.uk.ebi.pmc.persistence.repository.ErrorsRepository
import ac.uk.ebi.pmc.persistence.repository.SubmissionRepository
import ac.uk.ebi.scheduler.properties.PmcMode
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ExceptionUtils

private val logger = KotlinLogging.logger {}

class ErrorsDocService(
    private val errorsRepository: ErrorsRepository,
    private val subRepository: SubmissionRepository
) {

    suspend fun saveError(submission: SubmissionDoc, mode: PmcMode, throwable: Throwable) {
        logger.error { "Error processing submission ${submission.accNo} from file ${submission.sourceFile}, ${throwable.message}" }
        subRepository.update(submission.withStatus(getError(mode)))
        errorsRepository.save(SubmissionErrorDoc(submission, ExceptionUtils.getStackTrace(throwable), mode))
    }

    private fun getError(pmcMode: PmcMode) = when (pmcMode) {
        PmcMode.LOAD -> SubmissionStatus.ERROR_LOAD
        PmcMode.PROCESS -> SubmissionStatus.ERROR_PROCESS
        PmcMode.SUBMIT -> SubmissionStatus.ERROR_SUBMIT
    }

    suspend fun saveError(sourceFile: String, submissionBody: String, process: PmcMode, throwable: Throwable) {
        errorsRepository.save(SubmissionErrorDoc(sourceFile, submissionBody, ExceptionUtils.getStackTrace(throwable), process))
    }
}
