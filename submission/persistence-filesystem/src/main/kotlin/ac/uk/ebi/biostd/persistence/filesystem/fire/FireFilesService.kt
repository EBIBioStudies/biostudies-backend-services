package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.asFireFile
import ebi.ac.uk.extended.model.expectedPath
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.fileSequence
import uk.ac.ebi.fire.client.integration.web.FireClient

private val logger = KotlinLogging.logger {}

class FireFilesService(
    private val client: FireClient,
    private val serializationService: ExtSerializationService,
) : FilesService {
    /**
     * Get or persist the given ext file from FIRE. Note that this method assumes that all the fire files belonging to
     * previous submission versions have been unpublished and any file with a path is assumed to be already used.
     *
     * For both FIRE and NFS, the file is searched by md5, and the system checks that it does not belong to another
     * submission. The method also ensures that the file has no path (i.e. it was submitted in the same submission in a
     * different path) and if so, even if the file exists in FIRE, it gets duplicated to ensure consistency. TODO:
     * handle scenario when the same file appear two times in the same submission and it was already in fire.
     */
    override fun persistSubmissionFile(sub: ExtSubmission, file: ExtFile): FireFile {
        return when (file) {
            is FireFile -> getOrCreate(file, sub.expectedPath(file))
            is NfsFile -> return getOrCreate(file, sub.expectedPath(file))
        }
    }

    private fun getOrCreate(
        fireFile: FireFile,
        expectedPath: String,
    ): FireFile {
        return when (val path = fireFile.firePath) {
            expectedPath -> fireFile
            null -> setMetadata(fireFile.fireId, fireFile, expectedPath, fireFile.published)
            else -> {
                val file = requireNotNull(client.downloadByPath(path)) { "Could not download file with path $path" }
                val saved = client.save(file, fireFile.md5, fireFile.size)
                setMetadata(saved.fireOid, fireFile, expectedPath, false)
            }
        }
    }

    private fun getOrCreate(file: NfsFile, expectedPath: String): FireFile {
        val matches = client.findByMd5(file.md5)
        val apiFile = matches.find { it.path == expectedPath }
            ?: matches.find { it.path == null }
            ?: client.save(file.file, file.md5, file.size)
        val fireFile = file.asFireFile(apiFile.fireOid, apiFile.path, apiFile.published)
        return getOrCreate(fireFile, expectedPath)
    }

    private fun setMetadata(fireOid: String, file: ExtFile, expectedPath: String, published: Boolean): FireFile {
        client.setPath(fireOid, expectedPath)
        return file.asFireFile(fireOid, expectedPath, published)
    }

    override fun deleteSubmissionFile(sub: ExtSubmission, file: ExtFile) {
        require(file is FireFile) { "FireFilesService should only handle FireFile" }
        client.delete(file.fireId)
    }

    override fun deleteFtpFile(sub: ExtSubmission, file: ExtFile) {
        // No need to delete FTP links on FIRE as file deleting complete this
    }

    override fun deleteSubmissionFiles(sub: ExtSubmission) {
        fun deleteFile(index: Int, file: FireFile) {
            logger.info { "${sub.accNo} ${sub.owner} Deleting file $index, path='${file.filePath}'" }
            deleteSubmissionFile(sub, file)
        }

        logger.info { "${sub.accNo} ${sub.owner} Started cleaning submission files for ${sub.accNo}" }
        serializationService
            .fileSequence(sub)
            .filterIsInstance(FireFile::class.java)
            .forEachIndexed { index, file -> deleteFile(index, file) }
        logger.info { "${sub.accNo} ${sub.owner} Finished cleaning submission files for ${sub.accNo}" }
    }
}
