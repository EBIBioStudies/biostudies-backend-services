package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.exception.UserGroupNotFound
import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.model.UserFile
import ac.uk.ebi.biostd.files.utils.transferTo
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RW_RW____
import ebi.ac.uk.io.ext.listFilesOrEmpty
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile
import java.io.File

class PathFilesService(
    private val basePath: File,
) : FileService {

    companion object {

        fun forUserGroup(user: SecurityUser, groupName: String): FileService {
            return PathFilesService(getGroupPath(user, groupName))
        }

        fun forUser(user: SecurityUser): PathFilesService {
            return PathFilesService(user.magicFolder.path.toFile())
        }

        private fun getGroupPath(user: SecurityUser, groupName: String): File {
            val group = user.groupsFolders
                .find { it.groupName == groupName }
                ?: throw UserGroupNotFound(user, groupName)
            return group.path.toFile()
        }
    }

    override fun uploadFile(path: String, file: File) {
        FileUtils.copyOrReplaceFile(
            source = file,
            target = basePath.resolve(file.name),
            permissions = Permissions(RW_RW____, RWXRWX___)
        )
    }

    override fun uploadFiles(path: String, files: List<MultipartFile>) {
        files.forEach { file -> transferTo(basePath.resolve(path).toPath(), file) }
    }

    override fun getFile(path: String, fileName: String): File {
        val userFile = basePath.resolve(path).resolve(fileName)
        require(userFile.exists() && userFile.isFile) { "Invalid request $path is not a valid user file" }
        return userFile
    }

    override fun createFolder(path: String, folderName: String) {
        val folder = basePath.resolve(path).resolve(folderName)
        FileUtils.createEmptyFolder(folder.toPath(), RWXRWX___)
    }

    override fun listFiles(path: String): FilesSpec {
        val folder = basePath.resolve(path)
        val files = folder
            .listFilesOrEmpty()
            .map {
                UserFile(
                    name = it.name,
                    path = it.relativeTo(basePath).toString(),
                    fileSize = it.size(calculateDirectories = false),
                    type = UserFileType.getType(it)
                )
            }
        return FilesSpec(files)
    }

    override fun deleteFile(path: String, fileName: String) {
        val userFile = basePath.resolve(path).resolve(fileName)
        require(basePath != userFile.toPath()) { "Can not delete user root folder" }
        FileUtils.deleteFile(userFile)
    }
}
