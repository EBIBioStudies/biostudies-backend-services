package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.request.FilePersistenceRequest
import ac.uk.ebi.biostd.persistence.filesystem.request.Md5
import ac.uk.ebi.biostd.persistence.filesystem.service.processFiles
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
import mu.KotlinLogging
import uk.ac.ebi.fire.client.integration.web.FireWebClient

private val logger = KotlinLogging.logger {}

class FireFilesService(private val fireWebClient: FireWebClient) : FilesService {
    override fun persistSubmissionFiles(request: FilePersistenceRequest): ExtSubmission {
        val (sub, _, previousFiles) = request
        logger.info { "${sub.accNo} ${sub.submitter} Persisting files of submission ${sub.accNo} on FIRE" }

        val config = FireFileProcessingConfig(sub.accNo, sub.submitter, sub.relPath, fireWebClient, previousFiles)
        val processed = processFiles(sub) { config.processFile(request.submission, it) }

        logger.info { "${sub.accNo} ${sub.submitter} Finished persisting files of submission ${sub.accNo} on FIRE" }

        return processed
    }
}

data class FireFileProcessingConfig(
    val accNo: String,
    val submitter: String,
    val relPath: String,
    val fireWebClient: FireWebClient,
    val previousFiles: Map<Md5, ExtFile>
)

fun FireFileProcessingConfig.processFile(
    sub: ExtSubmission,
    file: ExtFile
): ExtFile = if (file is NfsFile) processNfsFile(sub.relPath, file) else file

fun FireFileProcessingConfig.processNfsFile(relPath: String, nfsFile: NfsFile): ExtFile {
    logger.info { "$accNo $submitter Persisting file ${nfsFile.fileName} with size ${nfsFile.file.size()} on FIRE" }

    val fileFire = previousFiles[nfsFile.md5] as FireFile?

    return if (fileFire == null) saveFile(relPath, nfsFile) else reusePreviousFile(fileFire, nfsFile)
}

private fun reusePreviousFile(fireFile: FireFile, nfsFile: NfsFile) =
    FireFile(nfsFile.file.name, nfsFile.fileName, fireFile.fireId, fireFile.md5, fireFile.size, nfsFile.attributes)

private fun FireFileProcessingConfig.saveFile(relPath: String, nfsFile: NfsFile) =
    if (nfsFile.file.isDirectory) fireDirectory(nfsFile) else persistFireFile(relPath, nfsFile)

private fun fireDirectory(file: NfsFile) = FireDirectory(file.fileName, file.md5, file.size, file.attributes)

private fun FireFileProcessingConfig.persistFireFile(relPath: String, nfsFile: NfsFile): FireFile {
    val store = fireWebClient.save(nfsFile.file, nfsFile.md5, relPath)
    return FireFile(
        nfsFile.file.name,
        nfsFile.fileName,
        store.fireOid,
        store.objectMd5,
        store.objectSize.toLong(),
        nfsFile.attributes
    )
}
