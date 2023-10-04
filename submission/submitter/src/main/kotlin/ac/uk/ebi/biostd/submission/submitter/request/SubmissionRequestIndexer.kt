package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import kotlinx.coroutines.flow.asFlow
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class SubmissionRequestIndexer(
    private val extSerializationService: ExtSerializationService,
    private val requestService: SubmissionRequestPersistenceService,
    private val filesRequestService: SubmissionRequestFilesPersistenceService,
) {
    /**
     * Index submission request file by creating records for each one. Note that pagetab files are processed first,
     * guarantee by @see uk.ac.ebi.extended.serialization.service.ExtSerializationService.fileSequence to reduce time
     * submission main/core data is not available.
     */
    suspend fun indexRequest(accNo: String, version: Int) {
        val request = requestService.getPendingRequest(accNo, version)
        val sub = request.submission

        logger.info { "${sub.accNo} ${sub.owner} Started indexing submission files" }
        val totalFiles = indexSubmissionFiles(sub)
        requestService.saveSubmissionRequest(request.indexed(totalFiles))

        logger.info { "${sub.accNo} ${sub.owner} Finished indexing submission files" }
    }

    private suspend fun indexSubmissionFiles(sub: ExtSubmission): Int {
        val elements = AtomicInteger(0)
        extSerializationService
            .fileSequence(sub)
            .mapIndexed { idx, file -> SubmissionRequestFile(sub.accNo, sub.version, idx + 1, file.filePath, file) }
            .asFlow()
            .collect {
                logger.info { "${sub.accNo} ${sub.owner} Indexing submission file ${it.index}, path='${it.path}'" }
                filesRequestService.saveSubmissionRequestFile(it)
                elements.incrementAndGet()
            }
        return elements.get()
    }
}
