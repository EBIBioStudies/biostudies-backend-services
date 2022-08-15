package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FileSystemService(
    private val storageService: FileStorageService,
) {
    fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
        logger.info { "${sub.accNo} ${sub.owner} Processing files of submission ${sub.accNo}" }
        val processedSubmission = storageService.persistSubmissionFiles(sub)
        val finalSub = storageService.generatePageTab(processedSubmission)
        logger.info { "${sub.accNo} ${sub.owner} Finished processing files of submission ${sub.accNo}" }
        return finalSub
    }

    fun cleanFolder(previousSubmission: ExtSubmission) {
        storageService.cleanSubmissionFiles(previousSubmission)
    }
}
