package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.UserPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
@Api(tags = ["User Files"])
class UserFilesResource(
    private val filesMapper: FilesMapper,
    private val fileManager: UserFilesService
) {
    @GetMapping("/files/user/**")
    @ResponseBody
    @ApiOperation("List the user directory files")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun listFiles(
        @BioUser user: SecurityUser,

        @ApiParam(name = "path", value = "Sub directory to look up inside the user directory")
        pathDescriptor: UserPath
    ) = filesMapper.asUserFiles(fileManager.listFiles(user, pathDescriptor.path))

    @GetMapping("/files/user/**", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    @ApiOperation("Download files from the user directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun downloadFile(
        @BioUser user: SecurityUser,

        @ApiParam(name = "fileName", value = "Name of the file to be downloaded")
        @RequestParam(name = "fileName") fileName: String,

        @ApiParam(name = "path", value = "Sub directory to look up inside the user directory")
        pathDescriptor: UserPath
    ): FileSystemResource = FileSystemResource(fileManager.getFile(user, pathDescriptor.path, fileName))

    @PostMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation("Upload files to the user directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun uploadFile(
        @BioUser user: SecurityUser,

        @ApiParam(name = "path", value = "Sub directory to look up inside the user directory")
        pathDescriptor: UserPath,

        @ApiParam(name = "files", value = "List of files to be uploaded")
        @RequestParam("files") files: Array<MultipartFile>
    ) = fileManager.uploadFiles(user, pathDescriptor.path, files.toList())

    @DeleteMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation("Delete a file from the user directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun deleteFile(
        @BioUser user: SecurityUser,

        @ApiParam(name = "fileName", value = "Name of the file to be deleted")
        @RequestParam(name = "fileName") fileName: String,

        @ApiParam(name = "path", value = "Sub directory to look up inside the user directory")
        pathDescriptor: UserPath
    ) = fileManager.deleteFile(user, pathDescriptor.path, fileName)

    @PostMapping("/folder/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation("Create a sub directory inside the user directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun createFolder(
        @BioUser user: SecurityUser,

        @ApiParam(name = "folder", value = "Name of the folder to be created")
        @RequestParam(name = "folder") folder: String,

        @ApiParam(name = "path", value = "Sub directory to look up inside the user directory")
        pathDescriptor: UserPath
    ) = fileManager.createFolder(user, pathDescriptor.path, folder)
}
