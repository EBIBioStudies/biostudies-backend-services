package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.model.DirFilePath
import ebi.ac.uk.model.FilePath
import ebi.ac.uk.model.RenameFilePath
import ebi.ac.uk.model.UserPath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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

@Suppress("TooManyFunctions")
@RestController
@PreAuthorize("isAuthenticated()")
@Tag(name = "User Files", description = "Browse and manage files in the authenticated user's submission workspace.")
class UserFilesResource(
    private val filesMapper: FilesMapper,
    private val fileServiceFactory: FileServiceFactory,
) {
    @GetMapping("/files/user/**")
    @ResponseBody
    @Operation(
        summary = "List User Files",
        description = "List files and folders at a path in the authenticated user's workspace.",
    )
    suspend fun listFiles(
        @BioUser user: SecurityUser,
        pathDescriptor: UserPath,
    ): List<UserFile> {
        val filesService = fileServiceFactory.forUser(user)
        return filesMapper.asUserFiles(filesService.listFiles(pathDescriptor.path))
    }

    @DeleteMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Delete User File",
        description = "Delete one file from a path in the authenticated user's workspace.",
    )
    suspend fun deleteFile(
        @BioUser user: SecurityUser,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.deleteFile(pathDescriptor.path, fileName)
    }

    @PostMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Upload User Files",
        description = "Upload one or more files into a path in the authenticated user's workspace.",
    )
    suspend fun uploadFile(
        @BioUser user: SecurityUser,
        pathDescriptor: UserPath,
        @RequestParam("files") files: Array<MultipartFile>,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.uploadFiles(pathDescriptor.path, files.toList())
    }

    @PostMapping("/folder/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Create User Folder",
        description = "Create a folder at a path in the authenticated user's workspace.",
    )
    suspend fun createFolder(
        @BioUser user: SecurityUser,
        @RequestParam(name = "folder") folder: String,
        pathDescriptor: UserPath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.createFolder(pathDescriptor.path, folder)
    }

    @GetMapping("/files/user/download", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    @Operation(
        summary = "Download User File",
        description = "Download one file from the authenticated user's workspace using query parameters.",
    )
    suspend fun downloadFile(
        @BioUser user: SecurityUser,
        @RequestParam(name = "fileName") fileName: String,
        @RequestBody filePath: FilePath,
    ): ResponseEntity<FileSystemResource> =
        withContext(Dispatchers.IO) {
            val filesService = fileServiceFactory.forUser(user)
            val file = filesService.getFile(filePath.path, fileName)
            val fileResource = FileSystemResource(file)
            ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(file.toPath()) ?: "application/octet-stream")
                .body(fileResource)
        }

    @PostMapping("/files/user/query")
    @ResponseBody
    @Operation(
        summary = "Query User Files",
        description = "List files and folders using a request body path. This is the body-based variant of the listing API.",
    )
    suspend fun listFiles(
        @BioUser user: SecurityUser,
        @RequestBody filePath: FilePath,
    ): List<UserFile> {
        val filesService = fileServiceFactory.forUser(user)
        return filesMapper.asUserFiles(filesService.listFiles(filePath.path))
    }

    @PostMapping("/files/user/download", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    @ResponseBody
    @Operation(
        summary = "Download User File By Body",
        description = "Download one file using a request body path and file name.",
    )
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
    @Operation(
        summary = "Upload User Files By Body Path",
        description = "Upload files using the request parameter variant expected by the submission tool.",
    )
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
    @Operation(
        summary = "Delete User File By Body",
        description = "Delete one file using a request body path and file name.",
    )
    suspend fun deleteFile(
        @BioUser user: SecurityUser,
        @RequestBody filePath: DirFilePath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.deleteFile(filePath.path, filePath.fileName)
    }

    @PostMapping("/files/user/rename")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Rename User File",
        description = "Rename or move a file within the authenticated user's workspace.",
    )
    suspend fun renameFile(
        @BioUser user: SecurityUser,
        @RequestBody filePath: RenameFilePath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.renameFile(filePath.path, filePath.originalName, filePath.newName)
    }

    @PostMapping("/folder/user/create")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Create User Folder By Body",
        description = "Create a folder using a request body path and folder name.",
    )
    suspend fun createFolder(
        @BioUser user: SecurityUser,
        @RequestBody dirFilePath: DirFilePath,
    ) {
        val filesService = fileServiceFactory.forUser(user)
        filesService.createFolder(dirFilePath.path, dirFilePath.fileName)
    }
}
