package ac.uk.ebi.biostd.submission.domain.request

import ac.uk.ebi.biostd.persistence.common.model.RequestFileStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequest
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.RequestStatus.FILES_VALIDATED
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.filesFlow
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
    suspend fun indexRequest(
        accNo: String,
        version: Int,
        processId: String,
    ): SubmissionRequest =
        requestService.onRequest(accNo, version, FILES_VALIDATED, processId) {
            it.indexed(indexRequest(it.process!!.submission))
        }

    private suspend fun indexRequest(sub: ExtSubmission): Int {
        logger.info { "${sub.accNo} ${sub.owner} Started indexing submission files" }
        val totalFiles = indexSubmissionFiles(sub)
        logger.info { "${sub.accNo} ${sub.owner} Finished indexing submission files" }
        return totalFiles
    }

    private suspend fun indexSubmissionFiles(sub: ExtSubmission): Int {
        val elements = AtomicInteger(0)
        val paths = mutableSetOf<String>()

        extSerializationService
            .filesFlow(sub)
            .filterNot { paths.contains(it.filePath) }
            .onEach { paths.add(it.filePath) }
            .map { file -> SubmissionRequestFile(sub, file, INDEXED) }
            .collectIndexed { index, file ->
                logger.info { "${sub.accNo} ${sub.owner} Indexing submission file $index, path='${file.path}'" }
                filesRequestService.saveSubmissionRequestFile(file)
                elements.incrementAndGet()
            }
        return elements.get()
    }
}
