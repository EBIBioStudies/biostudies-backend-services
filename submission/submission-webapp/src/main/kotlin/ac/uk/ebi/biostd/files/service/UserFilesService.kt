package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.utils.copyFile
import ebi.ac.uk.io.asFileList
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile

class UserFilesService {

    fun uploadFiles(user: SecurityUser, path: String, files: Array<MultipartFile>) {
        val userPath = user.magicFolder.path
        files.forEach { file -> copyFile(userPath.resolve(path), file) }
    }

    fun listFiles(user: SecurityUser, path: String): FilesSpec {
        val userPath = user.magicFolder.path
        val file = userPath.resolve(path).toFile()
        return FilesSpec(userPath, file.asFileList())
    }

    fun createFolder(user: SecurityUser, path: String, folderName: String) {
        val userPath = user.magicFolder.path
        val folder = userPath
            .resolve(path)
            .resolve(folderName)
        folder.toFile().mkdirs()
    }

    fun deleteFile(user: SecurityUser, path: String, fileName: String) {
        val userPath = user.magicFolder.path
        val file = userPath
            .resolve(path)
            .resolve(fileName)
            .toFile()
        file.deleteRecursively()
    }
}
