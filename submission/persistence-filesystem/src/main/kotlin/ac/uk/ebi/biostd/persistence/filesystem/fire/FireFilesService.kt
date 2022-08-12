package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.extended.serialization.service.forEachFile

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireService: FireService,
    private val fileProcessingService: FileProcessingService,
    private val serializationService: ExtSerializationService,
) : FilesService {
    override fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission = processFiles(sub)

    override fun cleanSubmissionFiles(sub: ExtSubmission) = cleanPreviousFiles(sub)

    private fun cleanPreviousFiles(sub: ExtSubmission) {
        fun cleanFile(file: FireFile, index: Int) {
            logger.debug { "${sub.accNo}, ${sub.version} Cleaning file $index, path='${file.filePath}'" }
            fireService.cleanFile(file)
            logger.debug { "${sub.accNo}, ${sub.version} Cleaning file $index, path='${file.filePath}'" }
        }

        logger.info { "${sub.accNo} ${sub.owner} Cleaning Current submission Folder for ${sub.accNo}" }
        serializationService.forEachFile(sub) { file, index -> if (file is FireFile) cleanFile(file, index) }
        logger.info { "${sub.accNo} ${sub.owner} Cleaning Ftp Folder for ${sub.accNo}" }
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
