package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FileStorageService
import ac.uk.ebi.biostd.persistence.filesystem.api.FireFilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFilesService
import ac.uk.ebi.biostd.persistence.filesystem.fire.FireFtpService
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.FileProcessingService
import uk.ac.ebi.extended.serialization.service.forEachFile

private val logger = KotlinLogging.logger {}

// TODO not needed
class FireStorageService(
    private val ftpService: FireFtpService,
    private val filesService: FireFilesService,
    private val fileProcessingService: FileProcessingService,
    private val serializationService: ExtSerializationService,
): FileStorageService {
    override fun cleanSubmissionFiles(sub: ExtSubmission) {
//        logger.info { "${sub.accNo} ${sub.owner} Started cleaning Current submission folder" }
//
//        serializationService.forEachFile(sub) { file, index ->
//            if (file is FireFile) {
//                logger.info { "${sub.accNo}, ${sub.version} Cleaning file $index, path='${file.filePath}'" }
//                filesService.cleanSubmissionFile(file)
//            }
//        }
//
//        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning Current submission folder" }
        TODO()
    }

    override fun persistSubmissionFiles(sub: ExtSubmission): ExtSubmission {
//        logger.info { "${sub.accNo} ${sub.owner} Started persisting files of submission ${sub.accNo} on FIRE" }
//
//        val submission = fileProcessingService.processFiles(sub) { file, index ->
//            logger.info { "${sub.accNo}, ${sub.version} Persisting file $index, path='${file.filePath}' on FIRE" }
//            filesService.persistSubmissionFile(FireFilePersistenceRequest(sub.accNo, sub.version, sub.relPath, file))
//        }
//
//        logger.info { "${sub.accNo} ${sub.owner} Finished persisting files of submission ${sub.accNo} on FIRE" }
//
//        return submission
        TODO()
    }

    override fun releaseSubmissionFiles(sub: ExtSubmission) {
//        logger.info { "${sub.accNo} ${sub.owner} Started processing FTP links over FIRE" }
//
//        serializationService.forEachFile(sub) { file, idx ->
//            if (file is FireFile) {
//                logger.info { "${sub.accNo}, ${sub.owner} Publishing file $idx, fireId='${file.fireId}'" }
//                ftpService.releaseSubmissionFile(file)
//            }
//        }
//
//        logger.info { "${sub.accNo} ${sub.owner} Finished processing FTP links over FIRE" }
        TODO()
    }
}
