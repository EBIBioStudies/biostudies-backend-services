package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.exception.UserFolderNotFound
import ac.uk.ebi.biostd.files.exception.UserGroupNotFound
import ac.uk.ebi.biostd.files.service.ftp.FtpFileService
import ac.uk.ebi.biostd.files.service.nfs.PathFilesService
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.biostd.ftp.FtpClient
import java.io.File
import java.nio.file.Files

class FileServiceFactory(private val ftpClient: FtpClient) {
    fun forUserGroup(
        user: SecurityUser,
        groupName: String,
    ): FileService {
        return PathFilesService(getGroupPath(user, groupName))
    }

    suspend fun forUser(user: SecurityUser): FileService =
        withContext(Dispatchers.IO) {
            val folder = user.userFolder
            if (!Files.isDirectory(folder.path)) throw UserFolderNotFound()

            when (folder) {
                is FtpUserFolder -> FtpFileService(folder.relativePath, ftpClient)
                is NfsUserFolder -> PathFilesService(folder.path.toFile())
            }
        }

    private fun getGroupPath(
        user: SecurityUser,
        groupName: String,
    ): File {
        val group =
            user.groupsFolders
                .find { it.groupName == groupName }
                ?: throw UserGroupNotFound(user, groupName)
        return group.path.toFile()
    }
}
