package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.persistFireFile
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.FileProcessingService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireWebClient: FireWebClient,
    private val fileProcessingService: FileProcessingService
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (sub, _) = request
        logger.info { "${sub.accNo} ${sub.owner} Persisting files of submission ${sub.accNo} on FIRE" }

        cleanSubmissionFolder(sub.accNo)
        val config = FireFileProcessingConfig(sub.accNo, sub.owner, sub.relPath, fireWebClient)
        val processed = fileProcessingService.processFiles(sub) { config.processFile(request.submission, it) }

        logger.info { "${sub.accNo} ${sub.owner} Finished persisting files of submission ${sub.accNo} on FIRE" }

        return processed
    }

    private fun cleanSubmissionFolder(accNo: String) {
        fireWebClient
            .findByAccNo(accNo)
            .forEach { fireWebClient.unsetPath(it.fireOid) }
    }
}

data class FireFileProcessingConfig(
    val accNo: String,
    val owner: String,
    val relPath: String,
    val fireWebClient: FireWebClient
)

fun FireFileProcessingConfig.processFile(sub: ExtSubmission, file: ExtFile): ExtFile = when (file) {
    is NfsFile -> processNfsFile(sub.relPath, file)
    is FireFile -> reuseFireFile(file, sub.relPath)
    is FireDirectory -> file
}

fun FireFileProcessingConfig.processNfsFile(relPath: String, nfsFile: NfsFile): ExtFile {
    logger.info { "$accNo $owner Persisting file ${nfsFile.fileName} with size ${nfsFile.file.size()} on FIRE" }

    return when {
        nfsFile.file.isDirectory -> persistFireDirectory(nfsFile)
        else -> persistFireFile(accNo, relPath, nfsFile)
    }
}

private fun persistFireDirectory(nfsFile: NfsFile): FireDirectory {
    val (filePath, relPath, file, _, _, _, attributes) = nfsFile
    return FireDirectory(filePath, relPath, file.md5(), file.size(), attributes)
}

private fun FireFileProcessingConfig.persistFireFile(accNo: String, subRelPath: String, nfsFile: NfsFile): FireFile {
    val (filePath, relPath, _, _, _, _, attributes) = nfsFile
    val fireFile = fireWebClient.persistFireFile(accNo, nfsFile.file, nfsFile.md5, "$subRelPath/$relPath")

    return FireFile(filePath, relPath, fireFile.fireOid, fireFile.objectMd5, fireFile.objectSize.toLong(), attributes)
}

private fun FireFileProcessingConfig.reuseFireFile(fireFile: FireFile, subRelPath: String): FireFile {
    fireWebClient.setPath(fireFile.fireId, "$subRelPath/${fireFile.relPath}")
    return fireFile
}
