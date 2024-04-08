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
    override fun uploadFile(
        path: String,
        file: File,
    ) {
        ftp.uploadFile(basePath.resolve(path).resolve(file.name)) { file.inputStream() }
    }

    override fun uploadFiles(
        path: String,
        files: List<MultipartFile>,
    ) {
        val ftpFiles = files.map { basePath.resolve(path).resolve(it.originalFilename!!) to { it.inputStream } }
        ftp.uploadFiles(basePath.resolve(path), ftpFiles)
    }

    override fun getFile(
        path: String,
        fileName: String,
    ): File {
        val target = Files.createTempFile(path, fileName)
        val ftpPath = basePath.resolve(path).resolve(fileName)

        target.outputStream().use { ftp.downloadFile(ftpPath, it) }
        return target.toFile()
    }

    override fun createFolder(
        path: String,
        folderName: String,
    ) {
        ftp.createFolder(basePath.resolve(path))
    }

    override fun listFiles(path: String): FilesSpec {
        val files =
            ftp
                .listFiles(basePath.resolve(path))
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

    override fun deleteFile(
        path: String,
        fileName: String,
    ) {
        ftp.deleteFile(basePath.resolve(path).resolve(fileName))
    }
}
