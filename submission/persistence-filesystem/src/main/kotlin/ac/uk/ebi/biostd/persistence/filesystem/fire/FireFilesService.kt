package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ebi.ac.uk.extended.model.ExtSubmission
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

        logger.info { "${sub.accNo} ${sub.owner} Persisting files of submission ${sub.accNo} on FIRE" }
        val processed = processFiles(request.submission)
        logger.info { "${sub.accNo} ${sub.owner} Finished persisting files of submission ${sub.accNo} on FIRE" }

        return processed
    }

    private fun processFiles(sub: ExtSubmission): ExtSubmission =
        fileProcessingService.processFiles(sub) { file, index ->
            logger.debug { "${sub.accNo}, ${sub.version} Processing file $index, path='${file.filePath}'" }
            fireService.getOrPersist(sub, file)
        }
}
