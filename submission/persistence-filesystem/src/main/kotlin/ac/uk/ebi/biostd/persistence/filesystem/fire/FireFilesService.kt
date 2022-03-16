package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.request.Md5
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
    private val fileProcessingService: FileProcessingService,
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (sub, _, previousFiles) = request
        logger.info { "${sub.accNo} ${sub.owner} Persisting files of submission ${sub.accNo} on FIRE" }

        val config = FireFileProcessingConfig(sub.accNo, sub.owner, sub.relPath, fireWebClient, previousFiles)
        val processed = fileProcessingService.processFiles(sub) { config.processFile(request.submission, it) }

        logger.info { "${sub.accNo} ${sub.owner} Finished persisting files of submission ${sub.accNo} on FIRE" }

        return processed
    }
}

data class FireFileProcessingConfig(
    val accNo: String,
    val owner: String,
    val relPath: String,
    val fireWebClient: FireWebClient,
    val previousFiles: Map<Md5, ExtFile>,
)

fun FireFileProcessingConfig.processFile(sub: ExtSubmission, file: ExtFile): ExtFile =
    if (file is NfsFile) processNfsFile(sub.relPath, file) else file

fun FireFileProcessingConfig.processNfsFile(relPath: String, nfsFile: NfsFile): ExtFile {
    logger.info { "$accNo $owner Persisting file ${nfsFile.fileName} with size ${nfsFile.file.size()} on FIRE" }

    val fileFire = previousFiles[nfsFile.md5] as FireFile?

    return if (fileFire == null) saveFile(relPath, nfsFile) else reusePreviousFile(fileFire, nfsFile)
}

private fun reusePreviousFile(fireFile: FireFile, nfsFile: NfsFile) =
    FireFile(
        nfsFile.filePath,
        nfsFile.relPath,
        fireFile.fireId,
        fireFile.md5,
        fireFile.size,
        nfsFile.attributes
    )

private fun FireFileProcessingConfig.saveFile(subRelPath: String, nfsFile: NfsFile): ExtFile {
    val (filePath, relPath, file, _, _, _, attributes) = nfsFile

    return when {
        nfsFile.file.isDirectory -> FireDirectory(filePath, relPath, file.md5(), file.size(), attributes)
        else -> {
            val store = fireWebClient.save(nfsFile.file, nfsFile.md5, "$subRelPath/${nfsFile.relPath}")
            FireFile(filePath, relPath, store.fireOid, store.objectMd5, store.objectSize.toLong(), attributes)
        }
    }
}
