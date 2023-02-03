package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.exception.UserGroupNotFound
import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.utils.transferTo
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path

class GroupFilesService {
    fun listFiles(groupName: String, user: SecurityUser, path: String): FilesSpec {
        val groupPath = getGroupPath(groupName, user)
        val file = groupPath.resolve(path).toFile()
        return FilesSpec(groupPath, file.asFileList())
    }

    fun uploadFiles(groupName: String, user: SecurityUser, path: String, files: Array<MultipartFile>) {
        val groupPath = getGroupPath(groupName, user)
        files.forEach { file -> transferTo(groupPath.resolve(path), file) }
    }

    fun getFile(groupName: String, user: SecurityUser, path: String, fileName: String): File {
        val groupPath = getGroupPath(groupName, user)
        val groupFile = groupPath.resolve(path).resolve(fileName).toFile()
        require(groupFile.exists() && groupFile.isFile) { "Invalid request $path is not a valid $groupName file" }
        return groupFile
    }

    fun createFolder(groupName: String, user: SecurityUser, path: String, folderName: String) {
        val groupPath = getGroupPath(groupName, user)
        FileUtils.createEmptyFolder(groupPath.resolve(path).resolve(folderName), RWXRWX___)
    }

    fun deleteFile(groupName: String, user: SecurityUser, path: String, fileName: String) {
        val groupPath = getGroupPath(groupName, user)
        val file = groupPath
            .resolve(path)
            .resolve(fileName)
            .toFile()
        FileUtils.deleteFile(file)
    }

    private fun getGroupPath(groupName: String, user: SecurityUser): Path {
        val group = user.groupsFolders.find { it.groupName == groupName } ?: throw UserGroupNotFound(user, groupName)
        return group.path
    }
}
