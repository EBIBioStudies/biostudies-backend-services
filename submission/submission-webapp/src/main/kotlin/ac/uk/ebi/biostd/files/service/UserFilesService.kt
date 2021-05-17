package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.utils.transferTo
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RW_RW____
import ebi.ac.uk.io.ext.asFileList
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile
import java.io.File

class UserFilesService {

    fun uploadFile(user: SecurityUser, path: String, file: File) {
        val folder = user.magicFolder.path.resolve(path)
        FileUtils.copyOrReplaceFile(
            source = file,
            target = folder.resolve(file.name).toFile(),
            filePermissions = RW_RW____,
            folderPermissions = RWXRWX___
        )
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
        FileUtils.createEmptyFolder(folder, RWXRWX___)
    }

    fun deleteFile(user: SecurityUser, path: String, fileName: String) {
        val userPath = user.magicFolder.path
        val userFile = userPath.resolve(path).resolve(fileName).toFile()
        require(userPath != userFile.toPath()) { "Can not delete user root folder" }
        FileUtils.deleteFile(userFile)
    }
}
