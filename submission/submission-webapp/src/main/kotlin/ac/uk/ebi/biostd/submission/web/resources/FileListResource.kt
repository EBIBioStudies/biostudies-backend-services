package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.service.SubmissionPersistenceQueryService
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.helpers.OnBehalfUtils
import ac.uk.ebi.biostd.submission.service.FileSourcesRequest
import ac.uk.ebi.biostd.submission.service.FileSourcesService
import ac.uk.ebi.biostd.submission.validator.filelist.FileListValidator
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.model.constants.PREVIOUS_VERSION_ACC_NO
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
    private val fileSourcesService: FileSourcesService,
    private val fileListValidator: FileListValidator,
    private val onBehalfUtils: OnBehalfUtils,
    private val submissionQueryService: SubmissionPersistenceQueryService,
) {
    // TODO change the parameter to just accNo
    // TODO move all the logic to FileListValidator
    // TODO add unit tests for FileSourceService (formerly known as SourceGenerator)
    @PostMapping("/validate")
    fun validateFileList(
        @BioUser user: SecurityUser,
        onBehalfRequest: OnBehalfRequest?,
        @RequestParam(ROOT_PATH) rootPath: String?,
        @RequestParam(FILE_LIST_NAME) fileListName: String,
        @RequestParam(PREVIOUS_VERSION_ACC_NO) accNo: String?,
    ) {
        val onBehalfUser = onBehalfRequest?.let { onBehalfUtils.getOnBehalfUser(it) }
        val submission = accNo?.let { submissionQueryService.findExtByAccNo(accNo, includeFileListFiles = false) }
        val request = FileSourcesRequest(
            onBehalfUser = onBehalfUser,
            submitter = user,
            files = null,
            rootPath = rootPath,
            submission = submission,
            preferredSources = emptyList()
        )

        fileListValidator.validateFileList(fileListName, fileSourcesService.submissionSources(request))
    }
}
