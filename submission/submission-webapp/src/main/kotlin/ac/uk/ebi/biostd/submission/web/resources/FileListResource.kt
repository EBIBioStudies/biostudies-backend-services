package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.domain.helpers.SourceGenerator
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/submissions/fileLists")
class FileListResource(
    private val sourceGenerator: SourceGenerator,
    private val fileListValidator: FileListValidator,
    private val onBehalfUtils: OnBehalfUtils
) {
    @PostMapping("/validate")
    fun validateFileList(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(FILE_LIST_NAME) fileListName: String,
    ) {
        val onBehalfUser = onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) }
        val filesSource = sourceGenerator.submitterSources(user, onBehalfUser)

        fileListValidator.validateFileList(fileListName, filesSource)
    }
}
