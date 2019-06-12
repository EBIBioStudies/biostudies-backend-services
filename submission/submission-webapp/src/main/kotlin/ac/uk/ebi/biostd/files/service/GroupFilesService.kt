package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.exception.UserGroupNotFound
import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.utils.copyFile
import ebi.ac.uk.io.asFileList
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path

class GroupFilesService {
    fun listFiles(groupName: String, user: SecurityUser, path: String): FilesSpec {
        val groupPath = getGroupPath(groupName, user)
        val file = groupPath.resolve(path).toFile()
        return FilesSpec(groupPath, file.asFileList())
    }

    fun uploadFiles(groupName: String, user: SecurityUser, path: String, files: Array<MultipartFile>) {
        val groupPath = getGroupPath(groupName, user)
        files.forEach { file -> copyFile(groupPath.resolve(path), file) }
    }

    fun createFolder(groupName: String, user: SecurityUser, path: String, folderName: String) {
        val groupPath = getGroupPath(groupName, user)
        val folder = groupPath
            .resolve(path)
            .resolve(folderName)
        folder.toFile().mkdirs()
    }

    fun deleteFile(groupName: String, user: SecurityUser, path: String, fileName: String) {
        val groupPath = getGroupPath(groupName, user)
        val file = groupPath
            .resolve(path)
            .resolve(fileName)
            .toFile()
        file.deleteRecursively()
    }

    private fun getGroupPath(groupName: String, user: SecurityUser): Path {
        val group = user.groupsFolders.find { it.groupName == groupName } ?: throw UserGroupNotFound(user, groupName)
        return group.path
    }
}
