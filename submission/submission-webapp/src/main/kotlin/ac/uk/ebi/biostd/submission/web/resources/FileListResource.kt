package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidationRequest
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.model.constants.ACC_NO
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.model.constants.ROOT_PATH
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/fileLists")
@Tag(name = "File Lists", description = "Validation helpers for submission file-list metadata before submission.")
class FileListResource(
    private val onBehalfUtils: OnBehalfUtils,
    private val fileListValidator: FileListValidator,
) {
    @PostMapping("/validate")
    @Operation(
        summary = "Validate File List",
        description =
            "Validate a file list against the files available to the current user or the requested on-behalf user. " +
                "Use this before submitting PageTab content that references an uploaded file list.",
    )
    suspend fun validateFileList(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfParameters?,
        @RequestParam(ACC_NO) accNo: String?,
        @RequestParam(ROOT_PATH) rootPath: String?,
        @RequestParam(FILE_LIST_NAME) fileListName: String,
    ) {
        val onBehalfUser = onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) }
        val request = FileListValidationRequest(accNo, rootPath, fileListName, user, onBehalfUser)
        fileListValidator.validateFileList(request)
    }
}
