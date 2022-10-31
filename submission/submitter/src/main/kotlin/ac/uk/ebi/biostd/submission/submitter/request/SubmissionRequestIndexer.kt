package ac.uk.ebi.biostd.submission.submitter.request

import ac.uk.ebi.biostd.persistence.common.model.RequestStatus.INDEXED
import ac.uk.ebi.biostd.persistence.common.model.SubmissionRequestFile
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestFilesPersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionRequestPersistenceService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
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
    fun indexRequest(accNo: String, version: Int) {
        val request = requestService.getPendingRequest(accNo, version)
        val sub = request.submission

        logger.info { "${sub.accNo} ${sub.owner} Started indexing submission files" }

        val totalFiles = indexSubmissionFiles(sub)
        requestService.updateRequestTotalFiles(accNo, version, totalFiles)
        requestService.updateRequestStatus(accNo, version, INDEXED)

        logger.info { "${sub.accNo} ${sub.owner} Finished indexing submission files" }
    }

    private fun indexSubmissionFiles(sub: ExtSubmission): Int {
        fun indexFile(file: ExtFile, index: Int) {
            logger.info { "${sub.accNo} ${sub.owner} Indexing submission file $index, path='${file.filePath}'" }
            val requestFile = SubmissionRequestFile(sub.accNo, sub.version, index, file.filePath, file)
            filesRequestService.saveSubmissionRequestFile(requestFile)
        }

        val index = AtomicInteger()
        return extSerializationService
            .fileSequence(sub)
            .onEach { indexFile(it, index.incrementAndGet()) }
            .count()
    }
}
