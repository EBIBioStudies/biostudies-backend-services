package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.getOrPersist
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
import org.zeroturnaround.zip.ZipUtil
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireTempDirPath: Path,
    private val fireWebClient: FireWebClient,
    private val fileProcessingService: FileProcessingService
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (sub, _) = request
        logger.info { "${sub.accNo} ${sub.owner} Persisting files of submission ${sub.accNo} on FIRE" }

        cleanSubmissionFolder(sub.accNo)
        val config = FireFileProcessingConfig(sub.accNo, sub.owner, sub.relPath, fireTempDirPath, fireWebClient)
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
    val subRelPath: String,
    val fireTempDirPath: Path,
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

private fun FireFileProcessingConfig.persistFireDirectory(nfsFile: NfsFile): FireDirectory {
    val (filePath, relPath, file, _, _, _, attributes) = nfsFile
    val compressed = fireTempDirPath.resolve(file.name).toFile()

    ZipUtil.pack(file, compressed)

    val fireDir = fireWebClient.getOrPersist(accNo, compressed, compressed.md5(), "$subRelPath/$relPath")

    return FireDirectory(filePath, relPath, fireDir.fireOid, fireDir.objectMd5, fireDir.objectSize.toLong(), attributes)
}

private fun FireFileProcessingConfig.persistFireFile(accNo: String, subRelPath: String, nfsFile: NfsFile): FireFile {
    val filePath = nfsFile.filePath
    val relPath = nfsFile.relPath
    val attributes = nfsFile.attributes
    val fireFile = fireWebClient.getOrPersist(accNo, nfsFile.file, nfsFile.md5, "$subRelPath/$relPath")
    return FireFile(filePath, relPath, fireFile.fireOid, fireFile.objectMd5, fireFile.objectSize.toLong(), attributes)
}

private fun FireFileProcessingConfig.reuseFireFile(fireFile: FireFile, subRelPath: String): FireFile {
    fireWebClient.setPath(fireFile.fireId, "$subRelPath/${fireFile.relPath}")
    return fireFile
}
