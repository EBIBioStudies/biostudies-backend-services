package ac.uk.ebi.biostd.files.service

import ac.uk.ebi.biostd.files.model.FilesSpec
import org.springframework.web.multipart.MultipartFile
import java.io.File

interface FileService {
    suspend fun uploadFile(
        path: String,
        file: File,
    )

    suspend fun uploadFiles(
        path: String,
        files: List<MultipartFile>,
    )

    suspend fun getFile(
        path: String,
        fileName: String,
    ): File

    suspend fun createFolder(
        path: String,
        folderName: String,
    )

    suspend fun listFiles(path: String): FilesSpec

    suspend fun deleteFile(
        path: String,
        fileName: String,
    )
}
