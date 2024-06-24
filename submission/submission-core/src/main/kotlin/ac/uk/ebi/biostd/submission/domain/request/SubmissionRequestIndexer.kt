package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.RqtUpdate
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.REQUESTED
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import mu.KotlinLogging
import uk.ac.ebi.events.service.EventsPublisherService
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionRequestIndexer(
    private val eventsPublisherService: EventsPublisherService,
    private val extSerializationService: ExtSerializationService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Index submission request file by creating records for each one. Note that pagetab files are processed first,
     * guarantee by @see uk.ac.ebi.extended.serialization.service.ExtSerializationService.fileSequence to reduce time
     * submission main/core data is not available.
     */
    suspend fun indexRequest(
        accNo: String,
        version: Int,
        processId: String,
    ) {
        requestService.onRequest(accNo, version, REQUESTED, processId) {
            var indexedRqt = it.indexed(indexRequest(it.submission))
            RqtUpdate(indexedRqt)
        }
        eventsPublisherService.requestIndexed(accNo, version)
    }

    private suspend fun indexRequest(sub: ExtSubmission): Int {
        logger.info { "${sub.accNo} ${sub.owner} Started indexing submission files" }
        val totalFiles = indexSubmissionFiles(sub)
        logger.info { "${sub.accNo} ${sub.owner} Finished indexing submission files" }
        return totalFiles
    }

    private suspend fun indexSubmissionFiles(sub: ExtSubmission): Int {
        val elements = AtomicInteger(0)
        extSerializationService
            .filesFlow(sub)
            .withIndex()
            .map { (idx, file) -> SubmissionRequestFile(sub, idx + 1, file, INDEXED) }
            .collect {
                logger.info { "${sub.accNo} ${sub.owner} Indexing submission file ${it.index}, path='${it.path}'" }
                filesRequestService.saveSubmissionRequestFile(it)
                elements.incrementAndGet()
            }
        return elements.get()
    }
}
