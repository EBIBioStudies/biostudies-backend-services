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

    /**
     * Deletes a file.
     * @throws ac.uk.ebi.biostd.files.exception.FileNotFoundException if the file does not exist (maps to HTTP 404)
     */
    suspend fun deleteFile(
        path: String,
        fileName: String,
    )

    /**
     * Renames a file from originalName to newName.
     * @throws ac.uk.ebi.biostd.files.exception.FileNotFoundException if the original file does not exist (maps to HTTP 404)
     * @throws ac.uk.ebi.biostd.files.exception.FileAlreadyExistsException if a file with the new name already exists (maps to HTTP 409)
     * @throws ac.uk.ebi.biostd.files.exception.FileOperationException if the rename operation fails (maps to HTTP 500)
     * @return true if the file was renamed successfully
     */
    suspend fun renameFile(
        path: String,
        originalName: String,
        newName: String,
    ): Boolean
}
