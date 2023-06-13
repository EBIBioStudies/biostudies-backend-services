package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import org.apache.commons.net.ftp.FTPSClient
import org.springframework.core.io.InputStreamSource
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.outputStream

class FtpFileService(
    private val basePath: Path,
    private val ftp: FTPSClient,
) : FileService {
    override fun uploadFile(path: String, file: File) {
        uploadFile(path, file)
    }

    override fun uploadFiles(path: String, files: List<MultipartFile>) {
        files.forEach { uploadFile(path, it) }
    }

    private fun uploadFile(path: String, source: InputStreamSource) {
        ftp.storeFile(basePath.resolve(path).toString(), source.inputStream)
    }

    override fun getFile(path: String, fileName: String): File {
        val target = Files.createTempFile(path, fileName)
        ftp.retrieveFile(basePath.resolve(path).toString(), target.outputStream())
        return target.toFile()
    }

    override fun createFolder(path: String, folderName: String) {
        ftp.makeDirectory(basePath.resolve(path).toString())
    }

    override fun listFiles(path: String): FilesSpec {
        TODO()
    }

    override fun deleteFile(path: String, fileName: String) {
        TODO("Not yet implemented")
    }

}
