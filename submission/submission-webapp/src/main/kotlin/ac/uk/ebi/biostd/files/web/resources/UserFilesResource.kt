package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilePath
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.UserPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
    @GetMapping("/files/user/**")
    @ResponseBody
    suspend fun listFiles(
        @BioUser user: SecurityUser,
        pathDescriptor: UserPath,
    ): List<UserFile> {
        val filesService = fileServiceFactory.forUser(user)
        return filesMapper.asUserFiles(filesService.listFiles(pathDescriptor.path))
    }

    @PostMapping("/files/query")
    @ResponseBody
    suspend fun listFiles(
        @BioUser user: SecurityUser,
        @RequestBody filePath: FilePath,
    ): List<UserFile> {
        val filesService = fileServiceFactory.forUser(user)
        return filesMapper.asUserFiles(filesService.listFiles(filePath.path))
    }

    @GetMapping("/files/user/**", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    suspend fun downloadFile(
        @BioUser user: SecurityUser,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath,
    ): ResponseEntity<FileSystemResource> =
        withContext(Dispatchers.IO) {
            val filesService = fileServiceFactory.forUser(user)
            val file = filesService.getFile(pathDescriptor.path, fileName)
            val fileResource = FileSystemResource(file)
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file.toPath()) ?: "application/octet-stream")
                .body(fileResource)
        }

    @PostMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun uploadFile(
        @BioUser user: SecurityUser,
        pathDescriptor: UserPath,
        @RequestParam("files") files: Array<MultipartFile>,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.uploadFiles(pathDescriptor.path, files.toList())
    }

    @DeleteMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun deleteFile(
        @BioUser user: SecurityUser,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.deleteFile(pathDescriptor.path, fileName)
    }

    @PostMapping("/folder/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun createFolder(
        @BioUser user: SecurityUser,
        @RequestParam(name = "folder") folder: String,
        pathDescriptor: UserPath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.createFolder(pathDescriptor.path, folder)
    }
}
