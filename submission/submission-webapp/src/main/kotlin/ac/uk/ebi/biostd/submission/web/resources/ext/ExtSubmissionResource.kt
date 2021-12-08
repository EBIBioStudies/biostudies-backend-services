package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.files.web.common.FileListPath
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ExtSubmissionService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ac.uk.ebi.biostd.submission.web.model.ExtPageRequest
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.model.constants.FILE_LISTS
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@RestController
@RequestMapping("/submissions/extended")
class ExtSubmissionResource(
    private val extPageMapper: ExtendedPageMapper,
    private val tempFileGenerator: TempFileGenerator,
    private val extSubmissionService: ExtSubmissionService,
    private val extSerializationService: ExtSerializationService
) {
    @GetMapping("/{accNo}")
    fun getExtended(@PathVariable accNo: String): ExtSubmission = extSubmissionService.getExtendedSubmission(accNo)

    @GetMapping("/{accNo}/referencedFiles/**")
    fun getReferencedFiles(
        @PathVariable accNo: String,
        fileListPath: FileListPath
    ): ExtFileTable = extSubmissionService.getReferencedFiles(accNo, fileListPath.path)

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun submitExtended(
        @BioUser user: SecurityUser,
        @RequestParam(FILE_LISTS, required = false) fileLists: Array<MultipartFile>?,
        @RequestParam(FILE_MODE, required = false) fileMode: FileMode = COPY,
        @RequestParam(SUBMISSION) extSubmission: String
    ): ExtSubmission = extSubmissionService.submitExt(
        user.email,
        extSerializationService.deserialize(extSubmission, ExtSubmission::class.java),
        fileLists?.let { tempFileGenerator.asFiles(it) } ?: emptyList(),
        fileMode
    )

    @PostMapping("/async")
    @PreAuthorize("isAuthenticated()")
    fun submitExtendedAsync(
        @BioUser user: SecurityUser,
        @RequestParam(FILE_LISTS, required = false) fileLists: Array<MultipartFile>?,
        @RequestParam(FILE_MODE, required = false) fileMode: FileMode = COPY,
        @RequestParam(SUBMISSION) extSubmission: String
    ) = extSubmissionService.submitExtAsync(
        user.email,
        extSerializationService.deserialize(extSubmission, ExtSubmission::class.java),
        fileLists?.let { tempFileGenerator.asFiles(it) } ?: emptyList(),
        fileMode
    )

    @GetMapping
    fun submissions(@ModelAttribute request: ExtPageRequest): ExtPage =
        extPageMapper.asExtPage(extSubmissionService.getExtendedSubmissions(request), request)
}
