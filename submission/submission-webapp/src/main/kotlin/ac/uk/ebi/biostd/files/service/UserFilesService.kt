package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import ebi.ac.uk.io.PermissionFileUtils
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

class UserFilesService {

    fun uploadFile(user: SecurityUser, path: String, file: File) {
        val folder = user.magicFolder.path.resolve(path)
        PermissionFileUtils.copyOrReplaceFile(file, folder.resolve(file.name).toFile(), FILE_PERMISSION)
    }

    fun uploadFiles(user: SecurityUser, path: String, files: List<MultipartFile>) {
        val userPath = user.magicFolder.path
        files.forEach { file -> transferTo(userPath.resolve(path), file) }
    }

    fun getFile(user: SecurityUser, path: String, fileName: String): File {
        val userPath = user.magicFolder.path
        val userFile = userPath.resolve(path).resolve(fileName).toFile()
        require(userFile.exists() && userFile.isFile) { "Invalid request $path is not a valid user file" }
        return userFile
    }

    fun listFiles(user: SecurityUser, path: String): FilesSpec {
        val userPath = user.magicFolder.path
        val file = userPath.resolve(path).toFile()
        return FilesSpec(userPath, file.asFileList())
    }

    fun createFolder(user: SecurityUser, path: String, folderName: String) {
        val userPath = user.magicFolder.path
        val folder = userPath.resolve(path).resolve(folderName)
        PermissionFileUtils.createFolder(folder, FILE_PERMISSION)
    }

    fun deleteFile(user: SecurityUser, path: String, fileName: String) {
        val userPath = user.magicFolder.path
        val userFile = userPath.resolve(path).resolve(fileName).toFile()
        require(userPath != userFile.toPath()) { "Can not delete user root folder" }
        userFile.deleteRecursively()
    }

    companion object {
        internal val FILE_PERMISSION = PosixFilePermissions.fromString("rwxrwxr--")

        internal fun transferTo(basePath: Path, file: MultipartFile) {
            val expectedFilePath = basePath.resolve(file.originalFilename)
            PermissionFileUtils.createParentFolders(expectedFilePath, FILE_PERMISSION)
            file.transferTo(expectedFilePath)
        }
    }
}
