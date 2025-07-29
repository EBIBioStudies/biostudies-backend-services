package ac.uk.ebi.biostd.persistence.filesystem.fire

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.RequestFile
import ebi.ac.uk.extended.model.asFireFile
import ebi.ac.uk.extended.model.expectedFirePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories

class FireFilesService(
    private val client: FireClient,
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
    override suspend fun persistSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ): FireFile =
        when (file) {
            is FireFile -> file
            is NfsFile -> getOrCreate(file, sub.expectedFirePath(file))
            is RequestFile -> error("RequestFile ${file.filePath} can not be persisted")
        }

    private suspend fun getOrCreate(
        file: NfsFile,
        expectedPath: String,
    ): FireFile {
        val apiFile = client.findByPath(expectedPath) ?: persistToFire(file, expectedPath)
        return file.asFireFile(apiFile.fireOid, apiFile.path!!, apiFile.published)
    }

    private suspend fun persistToFire(
        file: NfsFile,
        expectedPath: String,
    ): FireApiFile {
        val saved = client.save(file.file, file.md5, file.size)
        return client.setPath(saved.fireOid, expectedPath)
    }

    override suspend fun deleteSubmissionFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ) {
        require(file is FireFile) { "FireFilesService should only handle FireFile, '${file.filePath}' it is not" }
        client.delete(file.fireId)
    }

    override suspend fun deleteFtpFile(
        sub: ExtSubmissionInfo,
        file: ExtFile,
    ) {
        // No need to delete FTP links on FIRE as file deleting complete this
    }

    override suspend fun deleteEmptyFolders(sub: ExtSubmissionInfo) {
        // No need to delete FIRE empty bucket as they only exists as files are in them
    }

    override suspend fun copyFile(
        file: ExtFile,
        path: Path,
    ) {
        withContext(Dispatchers.IO) {
            require(file is FireFile) { "FireFilesService should only handle FireFile, '${file.filePath}' it is not" }
            val downloadedFile = client.downloadByPath(file.firePath)!!.toPath()

            path.createDirectories()
            Files.copy(downloadedFile, path, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
