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
        logger.info { "Started loading pending request accNo='$accNo', version='$version'" }
        val request = requestService.getPendingRequest(accNo, version)
        val sub = request.submission
        logger.info { "Finished loading pending request accNo='$accNo', version='$version'" }

        indexSubmissionFiles(sub)
        val totalFiles = filesRequestService.getSubmissionRequestFiles(accNo, version, sub.relPath, 0).count().toInt()
        requestService.updateRequestTotalFiles(accNo, version, totalFiles)
        requestService.updateRequestStatus(accNo, version, INDEXED)
    }

    private fun indexSubmissionFiles(sub: ExtSubmission) {
        fun indexFile(file: ExtFile, index: Int) {
            logger.info { "${sub.accNo} ${sub.owner} Indexing submission file $index, path='${file.filePath}'" }
            val requestFile = SubmissionRequestFile(sub.accNo, sub.version, index, file.filePath, file)
            filesRequestService.upsertSubmissionRequestFile(requestFile)
        }

        logger.info { "${sub.accNo} ${sub.owner} Started indexing submission files" }
        // TODO ignore incoming pagetab files
        val index = AtomicInteger()
        extSerializationService.fileSequence(sub).forEach { indexFile(it, index.incrementAndGet()) }
        logger.info { "${sub.accNo} ${sub.owner} Finished indexing submission files" }
    }
}
