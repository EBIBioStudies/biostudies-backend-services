package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.FileServiceFactory
import ac.uk.ebi.biostd.files.web.common.FilePath
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.GroupPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
class GroupFilesResource(
    private val filesMapper: FilesMapper,
    private val fileServiceFactory: FileServiceFactory,
) {
    @GetMapping("/files/groups/{groupName}/**")
    @ResponseBody
    suspend fun listGroupFiles(
        @BioUser user: SecurityUser,
        pathDescriptor: GroupPath,
        @PathVariable groupName: String,
    ): List<UserFile> {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        return filesMapper.asGroupFiles(groupName, groupService.listFiles(pathDescriptor.path))
    }

    @PostMapping("/files/groups/query")
    @ResponseBody
    suspend fun listGroupFiles(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestBody filePath: FilePath,
    ): List<UserFile> {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        return filesMapper.asGroupFiles(groupName, groupService.listFiles(filePath.path))
    }

    @GetMapping("/files/groups/{groupName}/**", produces = [APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    suspend fun downloadFile(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: GroupPath,
    ): FileSystemResource {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        return FileSystemResource(groupService.getFile(pathDescriptor.path, fileName))
    }

    @PostMapping("/files/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun uploadGroupFile(
        @BioUser user: SecurityUser,
        pathDescriptor: GroupPath,
        @PathVariable groupName: String,
        @RequestParam("files") files: Array<MultipartFile>,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.uploadFiles(pathDescriptor.path, files.toList())
    }

    @DeleteMapping("/files/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun deleteFile(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: GroupPath,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.deleteFile(pathDescriptor.path, fileName)
    }

    @PostMapping("/folder/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    suspend fun createFolder(
        @BioUser user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam(name = "folder") folder: String,
        pathDescriptor: GroupPath,
    ) {
        val groupService = fileServiceFactory.forUserGroup(user, groupName)
        groupService.createFolder(pathDescriptor.path, folder)
    }
}
