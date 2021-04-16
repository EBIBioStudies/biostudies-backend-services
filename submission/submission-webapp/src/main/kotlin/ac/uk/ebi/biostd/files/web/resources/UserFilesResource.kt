package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.UserPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile

@Controller
@PreAuthorize("isAuthenticated()")
class UserFilesResource(
    private val filesMapper: FilesMapper,
    private val fileManager: UserFilesService
) {
    @GetMapping("/files/user/**")
    @ResponseBody
    fun listFiles(
        @BioUser user: SecurityUser,
        pathDescriptor: UserPath
    ) = filesMapper.asUserFiles(fileManager.listFiles(user, pathDescriptor.path))

    @GetMapping("/files/user/**", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    fun downloadFile(
        @BioUser user: SecurityUser,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath
    ): ResponseEntity<FileSystemResource> {
        val fileSystemResource = FileSystemResource(fileManager.getFile(user, pathDescriptor.path, fileName))
        val contentDisposition = ContentDisposition.builder("inline").filename(fileSystemResource.filename).build()
        val headers = HttpHeaders().apply { setContentDisposition(contentDisposition) }

        return ResponseEntity.ok().headers(headers).contentLength(fileSystemResource.contentLength()).body(fileSystemResource)
    }

    @PostMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun uploadFile(
        @BioUser user: SecurityUser,
        pathDescriptor: UserPath,
        @RequestParam("files") files: Array<MultipartFile>
    ) = fileManager.uploadFiles(user, pathDescriptor.path, files.toList())

    @DeleteMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun deleteFile(
        @BioUser user: SecurityUser,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath
    ) = fileManager.deleteFile(user, pathDescriptor.path, fileName)

    @PostMapping("/folder/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun createFolder(
        @BioUser user: SecurityUser,
        @RequestParam(name = "folder") folder: String,
        pathDescriptor: UserPath
    ) = fileManager.createFolder(user, pathDescriptor.path, folder)
}
