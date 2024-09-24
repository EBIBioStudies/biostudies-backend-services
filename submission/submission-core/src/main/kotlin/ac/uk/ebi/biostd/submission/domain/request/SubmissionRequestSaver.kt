package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.service.RqtResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.copyWithAttributes
import ebi.ac.uk.model.RequestStatus.CHECK_RELEASED
import ebi.ac.uk.model.RequestStatus.PERSISTED
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService

private val logger = KotlinLogging.logger {}

class SubmissionRequestSaver(
    private val requestService: SubmissionRequestPersistenceService,
    private val fileProcessingService: FileProcessingService,
    private val persistenceService: SubmissionPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    suspend fun saveRequest(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest {
        val (rqt) =
            requestService.onRequest(accNo, version, CHECK_RELEASED, processId) {
                val sub = saveRequest(it.submission)
                RqtResponse(it.withNewStatus(PERSISTED), sub)
            }
        return rqt
        // if (rqt.silentMode.not()) eventsPublisherService.submissionSubmitted(submission.accNo, rqt.notifyTo)
        // eventsPublisherService.submissionPersisted(submission.accNo, submission.version)
    }

    private suspend fun saveRequest(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Started saving submission '${sub.accNo}', version={${sub.version}}" }
        val assembled = assembleSubmission(sub)
        persistenceService.expirePreviousVersions(sub.accNo)
        val saved = persistenceService.saveSubmission(assembled)
        logger.info { "${sub.accNo} ${sub.owner} Finished saving submission '${sub.accNo}', version={${sub.version}}" }
        return saved
    }

    private suspend fun assembleSubmission(sub: ExtSubmission): ExtSubmission {
        return fileProcessingService.processFiles(sub) { file ->
            val requestFile = filesRequestService.getSubmissionRequestFile(sub.accNo, sub.version, file.filePath)
            return@processFiles requestFile.file.copyWithAttributes(file.attributes)
        }
    }
}
