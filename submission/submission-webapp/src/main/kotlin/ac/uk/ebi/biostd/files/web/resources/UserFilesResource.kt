package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.model.DirFilePath
import ebi.ac.uk.model.FilePath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files

@RestController
@PreAuthorize("isAuthenticated()")
class UserFilesResource(
    private val filesMapper: FilesMapper,
    private val fileServiceFactory: FileServiceFactory,
) {
    @PostMapping("/files/user/query")
    @ResponseBody
    suspend fun listFiles(
        @BioUser user: SecurityUser,
        @RequestBody filePath: FilePath,
    ): List<UserFile> {
        val filesService = fileServiceFactory.forUser(user)
        return filesMapper.asUserFiles(filesService.listFiles(filePath.path))
    }

    @PostMapping("/files/user/download", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseBody
    suspend fun downloadFile(
        @BioUser user: SecurityUser,
        @RequestBody fileDirPath: DirFilePath,
    ): ResponseEntity<FileSystemResource> =
        withContext(Dispatchers.IO) {
            val filesService = fileServiceFactory.forUser(user)
            val file = filesService.getFile(fileDirPath.path, fileDirPath.fileName)
            val fileResource = FileSystemResource(file)
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${fileDirPath.fileName}\"")
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file.toPath()) ?: "application/octet-stream")
                .body(fileResource)
        }

    @PostMapping("/files/user/upload")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun uploadFile(
        @BioUser user: SecurityUser,
        @RequestParam("filePath") filePath: FilePath,
        @RequestParam("files") files: Array<MultipartFile>,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.uploadFiles(filePath.path, files.toList())
    }

    @PostMapping("/files/user/delete")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun deleteFile(
        @BioUser user: SecurityUser,
        @RequestBody filePath: DirFilePath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.deleteFile(filePath.path, filePath.fileName)
    }

    @PostMapping("/folder/user/create")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun createFolder(
        @BioUser user: SecurityUser,
        @RequestBody dirFilePath: DirFilePath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.createFolder(dirFilePath.path, dirFilePath.fileName)
    }
}
