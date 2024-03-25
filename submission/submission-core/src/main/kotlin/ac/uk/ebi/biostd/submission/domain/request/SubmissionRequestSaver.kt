package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.CHECK_RELEASED
import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PERSISTED
import ac.uk.ebi.biostd.persistence.common.service.RqtResponse
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.copyWithAttributes
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.FileProcessingService

private val logger = KotlinLogging.logger {}

class SubmissionRequestSaver(
    private val requestService: SubmissionRequestPersistenceService,
    private val fileProcessingService: FileProcessingService,
    private val persistenceService: SubmissionPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
    private val eventsPublisherService: EventsPublisherService,
) {
    suspend fun saveRequest(accNo: String, version: Int, processId: String): ExtSubmission {
        val (rqt, submission) = requestService.onRequest(accNo, version, CHECK_RELEASED, processId, {
            val sub = saveRequest(it.submission)
            RqtResponse(it.withNewStatus(PERSISTED), sub)
        })
        eventsPublisherService.submissionSubmitted(accNo, rqt.notifyTo)
        eventsPublisherService.submissionPersisted(accNo, version)
        return submission
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
