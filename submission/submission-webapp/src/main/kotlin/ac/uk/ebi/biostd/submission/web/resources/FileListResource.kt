package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidationRequest
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.model.constants.ACC_NO
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.model.constants.ROOT_PATH
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/fileLists")
class FileListResource(
    private val onBehalfUtils: OnBehalfUtils,
    private val fileListValidator: FileListValidator,
) {
    @PostMapping("/validate")
    fun validateFileList(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(ACC_NO) accNo: String?,
        @RequestParam(ROOT_PATH) rootPath: String?,
        @RequestParam(FILE_LIST_NAME) fileListName: String,
    ) {
        val onBehalfUser = onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) }
        val request = FileListValidationRequest(accNo, rootPath, fileListName, user, onBehalfUser)

        fileListValidator.validateFileList(request)
    }
}
