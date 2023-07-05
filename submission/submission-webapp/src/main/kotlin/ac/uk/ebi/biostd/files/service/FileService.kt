package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import org.springframework.web.multipart.MultipartFile
import java.io.File

interface FileService {
    fun uploadFile(path: String, file: File)
    fun uploadFiles(path: String, files: List<MultipartFile>)
    fun getFile(path: String, fileName: String): File
    fun createFolder(path: String, folderName: String)
    fun listFiles(path: String): FilesSpec
    fun deleteFile(path: String, fileName: String)
}
