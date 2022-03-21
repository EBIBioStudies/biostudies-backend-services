package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.extensions.persistFireFile
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.service.processFiles
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.fire.client.api.FIRE_BIO_ACC_NO
import uk.ac.ebi.fire.client.model.FireFile as ClientFireFile
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.MetadataEntry

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val fireWebClient: FireWebClient
) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (sub, _) = request
        logger.info { "${sub.accNo} ${sub.owner} Persisting files of submission ${sub.accNo} on FIRE" }

        cleanSubmissionFolder(sub.accNo)
        val config = FireFileProcessingConfig(sub.accNo, sub.owner, sub.relPath, fireWebClient)
        val processed = processFiles(sub) { config.processFile(request.submission, it) }

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

fun FireFileProcessingConfig.processFile(sub: ExtSubmission, file: ExtFile): ExtFile =
    if (file is NfsFile) processNfsFile(sub.accNo, sub.relPath, file) else file

fun FireFileProcessingConfig.processNfsFile(accNo: String, relPath: String, nfsFile: NfsFile): ExtFile {
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
    val fileFire = fireWebClient.findByMd5(nfsFile.md5).firstOrNull { it.belongsToSubmission(accNo) }

    return if (fileFire == null) persistFile(accNo, subRelPath, nfsFile) else reusePreviousFile(fileFire, nfsFile)
}

private fun ClientFireFile.belongsToSubmission(accNo: String) =
    metadata?.contains(MetadataEntry(FIRE_BIO_ACC_NO, accNo)) ?: false

private fun FireFileProcessingConfig.persistFile(accNo: String, subRelPath: String, nfsFile: NfsFile): FireFile {
    val (filePath, relPath, _, _, _, _, attributes) = nfsFile
    val fireFile = fireWebClient.persistFireFile(accNo, nfsFile.file, "$subRelPath/$relPath")

    return FireFile(filePath, relPath, fireFile.fireOid, fireFile.objectMd5, fireFile.objectSize.toLong(), attributes)
}

private fun reusePreviousFile(fireFile: ClientFireFile, nfsFile: NfsFile) =
    FireFile(
        nfsFile.filePath,
        nfsFile.relPath,
        fireFile.fireOid,
        fireFile.objectMd5,
        fireFile.objectSize.toLong(),
        nfsFile.attributes
    )
