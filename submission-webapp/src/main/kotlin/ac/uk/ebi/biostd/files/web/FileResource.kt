package ac.uk.ebi.biostd.files.web

import ac.uk.ebi.biostd.files.service.FileManager
import ebi.ac.uk.model.User
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/files")
@PreAuthorize("isAuthenticated()")
class FileResource(private val fileManager: FileManager) {

    @PostMapping("/user/**")
    @ResponseStatus(value = HttpStatus.OK)
    fun uploadFile(
        @AuthenticationPrincipal user: User,
        pathDescriptor: PathDescriptor,
        @RequestParam("files") files: Array<MultipartFile>
    ) =
        fileManager.uploadFiles(user, pathDescriptor.path, files)
}
