package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.GroupFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.GroupPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile

@Controller
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Group Files"])
class GroupFilesResource(
    private val groupService: GroupFilesService,
    private val filesMapper: FilesMapper
) {
    @GetMapping("/files/groups/{groupName}/**")
    @ResponseBody
    @ApiOperation("List the group directory files")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun listGroupFiles(
        @BioUser user: SecurityUser,

        @ApiParam(name = "pathDescriptor", value = "Sub directory to look up inside the group directory")
        pathDescriptor: GroupPath,

        @ApiParam(name = "groupName", value = "The name of the group to list the files")
        @PathVariable groupName: String
    ) = filesMapper.asGroupFiles(groupName, groupService.listFiles(groupName, user, pathDescriptor.path))

    @GetMapping("/files/groups/{groupName}/**", produces = [APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    @ApiOperation("Download a file from the group directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun downloadFile(
        @BioUser user: SecurityUser,

        @ApiParam(name = "groupName", value = "The name of the group to download the files")
        @PathVariable groupName: String,

        @ApiParam(name = "fileName", value = "Name of the file to be downloaded")
        @RequestParam(name = "fileName") fileName: String,

        @ApiParam(name = "pathDescriptor", value = "Sub directory to look up inside the group directory")
        pathDescriptor: GroupPath
    ): FileSystemResource = FileSystemResource(groupService.getFile(groupName, user, pathDescriptor.path, fileName))

    @PostMapping("/files/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation("Upload files to the group directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun uploadGroupFile(
        @BioUser user: SecurityUser,

        @ApiParam(name = "pathDescriptor", value = "Sub directory to look up inside the group directory")
        pathDescriptor: GroupPath,

        @ApiParam(name = "groupName", value = "The name of the group to upload the files")
        @PathVariable groupName: String,

        @ApiParam(name = "files", value = "List of files to be uploaded")
        @RequestParam("files") files: Array<MultipartFile>
    ) = groupService.uploadFiles(groupName, user, pathDescriptor.path, files)

    @DeleteMapping("/files/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation("Delete a file from the group directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun deleteFile(
        @BioUser user: SecurityUser,

        @ApiParam(name = "groupName", value = "The name of the group to delete the file")
        @PathVariable groupName: String,

        @ApiParam(name = "fileName", value = "Name of the file to be deleted")
        @RequestParam(name = "fileName") fileName: String,

        @ApiParam(name = "pathDescriptor", value = "Sub directory to look up inside the group directory")
        pathDescriptor: GroupPath
    ) = groupService.deleteFile(groupName, user, pathDescriptor.path, fileName)

    @PostMapping("/folder/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    @ApiOperation("Create a sub directory inside the group directory")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun createFolder(
        @BioUser user: SecurityUser,

        @ApiParam(name = "groupName", value = "The name of the group to create the folder")
        @PathVariable groupName: String,

        @ApiParam(name = "folder", value = "Name of the folder to be created")
        @RequestParam(name = "folder") folder: String,

        @ApiParam(name = "pathDescriptor", value = "Sub directory to look up inside the group directory")
        pathDescriptor: GroupPath
    ) = groupService.createFolder(groupName, user, pathDescriptor.path, folder)
}
