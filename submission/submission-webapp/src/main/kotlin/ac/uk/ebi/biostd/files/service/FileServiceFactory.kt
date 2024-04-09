package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.exception.UserGroupNotFound
import ac.uk.ebi.biostd.files.service.ftp.FtpFileService
import ac.uk.ebi.biostd.files.service.nfs.PathFilesService
import ebi.ac.uk.ftp.FtpClient
import ebi.ac.uk.security.integration.model.api.FtpUserFolder
import ebi.ac.uk.security.integration.model.api.NfsUserFolder
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class FileServiceFactory(private val ftpClient: FtpClient) {
    fun forUserGroup(
        user: SecurityUser,
        groupName: String,
    ): FileService {
        return PathFilesService(getGroupPath(user, groupName))
    }

    fun forUser(user: SecurityUser): FileService {
        return when (val folder = user.userFolder) {
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
