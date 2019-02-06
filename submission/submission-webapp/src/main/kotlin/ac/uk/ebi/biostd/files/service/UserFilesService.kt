package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.utils.copyFile
import ebi.ac.uk.io.asFileList
import ebi.ac.uk.model.User
import ebi.ac.uk.paths.FolderResolver
import org.springframework.web.multipart.MultipartFile

class UserFilesService(private val folderResolver: FolderResolver) {

    fun uploadFiles(user: User, path: String, files: Array<MultipartFile>) {
        val userPath = folderResolver.getUserMagicFolderPath(user.id, user.secretKey)
        files.forEach { file -> copyFile(userPath.resolve(path), file) }
    }

    fun listFiles(user: User, path: String): FilesSpec {
        val userPath = folderResolver.getUserMagicFolderPath(user.id, user.secretKey)
        val file = userPath.resolve(path).toFile()
        return FilesSpec(userPath, file.asFileList())
    }

    fun createFolder(user: User, path: String, folderName: String) {
        val userPath = folderResolver.getUserMagicFolderPath(user.id, user.secretKey)
        val folder = userPath
            .resolve(path)
            .resolve(folderName)
        folder.toFile().mkdirs()
    }

    fun deleteFile(user: User, path: String, fileName: String) {
        val userPath = folderResolver.getUserMagicFolderPath(user.id, user.secretKey)
        val file = userPath
            .resolve(path)
            .resolve(fileName)
            .toFile()
        file.deleteRecursively()
    }
}
