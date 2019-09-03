package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.GroupFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.GroupPath
import ac.uk.ebi.biostd.files.web.common.UserPath
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
class GroupFilesResource(
    private val groupService: GroupFilesService,
    private val filesMapper: FilesMapper
) {
    @GetMapping("/files/groups/{groupName}/**")
    @ResponseBody
    fun listGroupFiles(
        @AuthenticationPrincipal user: SecurityUser,
        pathDescriptor: GroupPath,
        @PathVariable groupName: String
    ) = filesMapper.asGroupFiles(groupName, groupService.listFiles(groupName, user, pathDescriptor.path))

    @GetMapping("/files/groups/{groupName}/**", produces = [APPLICATION_OCTET_STREAM_VALUE], params = ["fileName"])
    @ResponseBody
    fun downloadFile(
        @AuthenticationPrincipal user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath
    ): FileSystemResource = FileSystemResource(groupService.getFile(groupName, user, pathDescriptor.path, fileName))

    @PostMapping("/files/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun uploadGroupFile(
        @AuthenticationPrincipal user: SecurityUser,
        pathDescriptor: GroupPath,
        @PathVariable groupName: String,
        @RequestParam("files") files: Array<MultipartFile>
    ) = groupService.uploadFiles(groupName, user, pathDescriptor.path, files)

    @DeleteMapping("/files/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun deleteFile(
        @AuthenticationPrincipal user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath
    ) = groupService.deleteFile(groupName, user, pathDescriptor.path, fileName)

    @PostMapping("/folder/groups/{groupName}/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun createFolder(
        @AuthenticationPrincipal user: SecurityUser,
        @PathVariable groupName: String,
        @RequestParam(name = "folder") folder: String,
        pathDescriptor: UserPath
    ) = groupService.createFolder(groupName, user, pathDescriptor.path, folder)
}
