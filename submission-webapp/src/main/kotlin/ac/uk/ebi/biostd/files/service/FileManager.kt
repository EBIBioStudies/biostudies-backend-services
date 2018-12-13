package ac.uk.ebi.biostd.files.service

import ebi.ac.uk.model.User
import ebi.ac.uk.paths.FolderResolver
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class FileManager(private val folder: FolderResolver) {

    fun uploadFiles(user: User, path: String, files: Array<MultipartFile>) {
        val userPath = folder.getUserMagicFolderPath(user.id, user.secretKey)
        files.forEach { file -> uploadFile(userPath, path, file) }
    }

    private fun uploadFile(userPath: Path, filePath: String, file: MultipartFile) {
        val expectedFilePath = userPath
            .resolve(filePath)
            .resolve(file.originalFilename)
            .toFile()
        expectedFilePath.mkdirs()
        Files.copy(file.inputStream, expectedFilePath.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }
}
