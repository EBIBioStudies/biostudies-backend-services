package ac.uk.ebi.biostd.files.web.resources

import ac.uk.ebi.biostd.files.service.UserFilesService
import ac.uk.ebi.biostd.files.web.common.FilesMapper
import ac.uk.ebi.biostd.files.web.common.UserPath
import ebi.ac.uk.model.User
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    private val fileManager: UserFilesService,
    private val filesMapper: FilesMapper
) {

    @GetMapping("/files/user/**")
    @ResponseBody
    fun listFiles(
        @AuthenticationPrincipal user: User,
        pathDescriptor: UserPath
    ) = filesMapper.asUserFiles(fileManager.listFiles(user, pathDescriptor.path))

    @PostMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun uploadFile(
        @AuthenticationPrincipal user: User,
        pathDescriptor: UserPath,
        @RequestParam("files") files: Array<MultipartFile>
    ) = fileManager.uploadFiles(user, pathDescriptor.path, files)

    @DeleteMapping("/files/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun deleteFile(
        @AuthenticationPrincipal user: User,
        @RequestParam(name = "fileName") fileName: String,
        pathDescriptor: UserPath
    ) = fileManager.deleteFile(user, pathDescriptor.path, fileName)

    @PostMapping("/folder/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun createFolder(
        @AuthenticationPrincipal user: User,
        @RequestParam(name = "folder") folder: String,
        pathDescriptor: UserPath
    ) = fileManager.createFolder(user, pathDescriptor.path, folder)
}
