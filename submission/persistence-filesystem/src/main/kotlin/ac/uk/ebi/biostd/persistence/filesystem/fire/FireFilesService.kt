package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.FileProcessingService

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireService: FireService,
    private val fileProcessingService: FileProcessingService,
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val sub = request.submission

        logger.info { "${sub.accNo} ${sub.owner} Cleaning Ftp Folder for ${sub.accNo}" }
        fireService.cleanFtp(sub)
        logger.info { "${sub.accNo} ${sub.owner} Finished Ftp Folder for ${sub.accNo}" }
        val processed = processFiles(request.submission)
        return processed
    }

    private fun processFiles(sub: ExtSubmission): ExtSubmission {
        var newFilesSize = 0L
        var newFiles = 0

        fun processFile(file: ExtFile, index: Int): FireFile {
            logger.debug { "${sub.accNo}, ${sub.version} Processing file $index, path='${file.filePath}'" }
            val (fireFile, created) = fireService.getOrPersist(sub, file)
            if (created) {
                newFiles += 1
                newFilesSize += fireFile.size
            }
            logger.debug { "${sub.accNo}, ${sub.version} Finished processing file $index, path='${file.filePath}'" }
            return fireFile
        }

        logger.info { "${sub.accNo} ${sub.owner} Starting persisting files of submission ${sub.accNo} on FIRE" }
        val submission = fileProcessingService.processFiles(sub) { file, index -> processFile(file, index) }
        logger.info { "${sub.accNo} ${sub.owner} Processed $newFiles new files, $newFilesSize bytes on FIRE" }
        logger.info { "${sub.accNo} ${sub.owner} Finished persisting files of submission ${sub.accNo} on FIRE" }
        return submission
    }
}
