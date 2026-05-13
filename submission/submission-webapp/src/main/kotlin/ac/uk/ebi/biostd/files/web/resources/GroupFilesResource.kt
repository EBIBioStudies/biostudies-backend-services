package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.model.DirFilePath
import ebi.ac.uk.model.FilePath
import ebi.ac.uk.model.RenameFilePath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@PreAuthorize("isAuthenticated()")
@Tag(name = "Group Files", description = "Browse and manage files in workspaces shared through security groups.")
class GroupFilesResource(
    private val filesMapper: FilesMapper,
    private val fileServiceFactory: FileServiceFactory,
) {
    @PostMapping("/files/groups/{groupName}/query")
    @ResponseBody
    @Operation(
        summary = "Query Group Files",
        description = "List files and folders in a group workspace the authenticated user can access.",
    )
    suspend fun listGroupFiles(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestBody filePath: FilePath,
    ): List<UserFile> {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        return filesMapper.asGroupFiles(groupName, groupService.listFiles(filePath.path))
    }

    @PostMapping("/files/groups/{groupName}/download", produces = [APPLICATION_OCTET_STREAM_VALUE])
    @ResponseBody
    @Operation(
        summary = "Download Group File",
        description = "Download one file from a group workspace.",
    )
    suspend fun downloadFile(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestBody filePath: DirFilePath,
    ): FileSystemResource {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        return FileSystemResource(groupService.getFile(filePath.path, filePath.fileName))
    }

    @PostMapping("/files/groups/{groupName}/upload")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Upload Group Files",
        description = "Upload one or more files into a group workspace.",
    )
    suspend fun uploadGroupFile(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam("filePath") filePath: FilePath,
        @RequestParam("files") files: Array<MultipartFile>,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.uploadFiles(filePath.path, files.toList())
    }

    @PostMapping("/files/groups/{groupName}/delete")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Delete Group File",
        description = "Delete one file from a group workspace.",
    )
    suspend fun deleteFile(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestBody filePath: DirFilePath,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.deleteFile(filePath.path, filePath.fileName)
    }

    @PostMapping("/files/groups/{groupName}/rename")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Rename Group File",
        description = "Rename or move a file within a group workspace.",
    )
    suspend fun renameFile(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestBody filePath: RenameFilePath,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.renameFile(filePath.path, filePath.originalName, filePath.newName)
    }

    @PostMapping("/folder/groups/{groupName}/create")
    @ResponseStatus(value = HttpStatus.OK)
    @Operation(
        summary = "Create Group Folder",
        description = "Create a folder in a group workspace.",
    )
    suspend fun createFolder(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestBody filePath: DirFilePath,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.createFolder(filePath.path, filePath.fileName)
    }
}
