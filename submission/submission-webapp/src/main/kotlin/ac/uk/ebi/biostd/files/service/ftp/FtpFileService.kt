package ac.uk.ebi.biostd.files.service.ftp

import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.model.UserFile
import ac.uk.ebi.biostd.files.service.FileService
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.ftp.FtpClient
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.outputStream

class FtpFileService(
    private val basePath: Path,
    private val ftp: FtpClient,
) : FileService {
    override suspend fun uploadFile(
        path: String,
        file: File,
    ) {
        ftp.uploadFile(basePath.safeResolve(path).safeResolve(file.name)) { file.inputStream() }
    }

    override suspend fun uploadFiles(
        path: String,
        files: List<MultipartFile>,
    ) {
        val ftpFiles = files.map { basePath.safeResolve(path).safeResolve(it.originalFilename!!) to { it.inputStream } }
        ftp.uploadFiles(basePath.safeResolve(path), ftpFiles)
    }

    override suspend fun getFile(
        path: String,
        fileName: String,
    ): File {
        val target = Files.createTempFile(null, fileName)
        val ftpPath = basePath.safeResolve(path).safeResolve(fileName)

        target.outputStream().use { ftp.downloadFile(ftpPath, it) }
        return target.toFile()
    }

    override suspend fun createFolder(
        path: String,
        folderName: String,
    ) {
        ftp.createFolder(basePath.safeResolve(path))
    }

    override suspend fun listFiles(path: String): FilesSpec {
        val files =
            ftp
                .listFiles(basePath.safeResolve(path))
                .map {
                    UserFile(
                        name = it.name,
                        path = path,
                        fileSize = it.size,
                        type = if (it.isFile) UserFileType.FILE else UserFileType.DIR,
                    )
                }
        return FilesSpec(files)
    }

    override suspend fun deleteFile(
        path: String,
        fileName: String,
    ) {
        ftp.deleteFile(basePath.safeResolve(path).safeResolve(fileName))
    }

    override suspend fun renameFile(
        path: String,
        originalName: String,
        newName: String,
    ) {
        val sourcePath = basePath.safeResolve(path).safeResolve(originalName)
        val targetPath = basePath.safeResolve(path).safeResolve(newName)

        require(ftp.findFile(sourcePath) != null) { "The file to be renamed does not exist: $sourcePath" }
        require(ftp.findFile(targetPath) == null) {
            "The new name for the file already exists at $path, please choose a different name than $newName"
        }
        check(ftp.renameFile(sourcePath, newName)) { "Failed to rename file from '$originalName' to '$newName'" }
    }

    private fun Path.safeResolve(path: String): Path {
        val resolved = resolve(path)
        require(resolved.startsWith(basePath)) { "The user does not have permission for accessing path '$path'" }
        return resolved
    }
}
