package ac.uk.ebi.biostd.files.service.nfs

import ac.uk.ebi.biostd.files.model.FilesSpec
import ac.uk.ebi.biostd.files.model.UserFile
import ac.uk.ebi.biostd.files.service.FileService
import ac.uk.ebi.biostd.files.utils.transferTo
import ebi.ac.uk.api.UserFileType
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.io.Permissions
import ebi.ac.uk.io.RWXRWX___
import ebi.ac.uk.io.RW_RW____
import ebi.ac.uk.io.ext.listFilesOrEmpty
import ebi.ac.uk.io.ext.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.web.multipart.MultipartFile
import java.io.File

class PathFilesService internal constructor(
    private val basePath: File,
) : FileService {
    override suspend fun uploadFile(
        path: String,
        file: File,
    ) = withContext(Dispatchers.IO) {
        FileUtils.copyOrReplaceFile(
            source = file,
            target = basePath.safeResolve(file.name),
            permissions = Permissions(RW_RW____, RWXRWX___),
        )
    }

    override suspend fun uploadFiles(
        path: String,
        files: List<MultipartFile>,
    ) = withContext(Dispatchers.IO) {
        files.forEach { file -> transferTo(basePath.safeResolve(path).toPath(), file) }
    }

    override suspend fun getFile(
        path: String,
        fileName: String,
    ): File =
        withContext(Dispatchers.IO) {
            val userFile = basePath.safeResolve(path).safeResolve(fileName)
            require(userFile.exists() && userFile.isFile) { "Invalid request $path is not a valid user file" }
            userFile
        }

    override suspend fun createFolder(
        path: String,
        folderName: String,
    ) = withContext(Dispatchers.IO) {
        val folder = basePath.safeResolve(path).safeResolve(folderName)
        FileUtils.createEmptyFolder(folder.toPath(), RWXRWX___)
    }

    override suspend fun listFiles(path: String): FilesSpec =
        withContext(Dispatchers.IO) {
            val folder = basePath.safeResolve(path)

            val files =
                folder
                    .listFilesOrEmpty()
                    .map {
                        UserFile(
                            name = it.name,
                            path = it.parentFile.relativeTo(basePath).toString(),
                            fileSize = it.size(calculateDirectories = false),
                            type = UserFileType.getType(it),
                        )
                    }
            FilesSpec(files)
        }

    override suspend fun deleteFile(
        path: String,
        fileName: String,
    ) = withContext(Dispatchers.IO) {
        val userFile = basePath.safeResolve(path).safeResolve(fileName)
        FileUtils.deleteFile(userFile)
    }

    private fun File.safeResolve(path: String): File {
        val resolved = resolve(path)
        require(resolved.startsWith(basePath)) { "The user does not have permission for accessing path '$path'" }
        return resolved
    }
}
