package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.PROCESSED
import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
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
    fun saveRequest(accNo: String, version: Int): ExtSubmission {
        val request = requestService.getCheckReleased(accNo, version)
        val sub = request.submission

        logger.info { "$accNo ${sub.owner} Started saving submission '${sub.accNo}', version={${sub.version}}" }
        val assemble = assembleSubmission(sub)
        persistenceService.expirePreviousVersions(sub.accNo)
        val saved = persistenceService.saveSubmission(assemble)
        logger.info { "$accNo ${sub.owner} Finished saving submission '${sub.accNo}', version={${sub.version}}" }

        requestService.saveSubmissionRequest(request.withNewStatus(PROCESSED))
        eventsPublisherService.submissionSubmitted(accNo, request.notifyTo)
        return saved
    }

    private fun assembleSubmission(sub: ExtSubmission): ExtSubmission {
        return fileProcessingService.processFiles(sub) { file ->
            val requestFile = filesRequestService.getSubmissionRequestFile(sub.accNo, sub.version, file.filePath)
            return@processFiles requestFile.file
        }
    }
}
