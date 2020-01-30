package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ac.uk.ebi.biostd.submission.web.handlers.ProjectWebHandler
import ac.uk.ebi.biostd.submission.web.handlers.SubmissionWebHandler
import ebi.ac.uk.model.Project
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.PROJECT
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/projects")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Projects"])
class ProjectResource(
    private val projectService: ProjectService,
    private val tmpFileGenerator: TempFileGenerator,
    private val projectWebHandler: ProjectWebHandler,
    private val tempFileGenerator: TempFileGenerator,
    private val submissionWebHandler: SubmissionWebHandler
) {
    @GetMapping
    @ResponseBody
    @ApiOperation("Get the list of available projects for the current user")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun getUserProjects(
        @BioUser
        user: SecurityUser
    ): List<Project> = projectService.getAllowedProjects(user, AccessType.ATTACH)

    @PostMapping(headers = ["${HttpHeaders.CONTENT_TYPE}=$MULTIPART_FORM_DATA"], produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Register a new project")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submit(
        @BioUser
        user: SecurityUser,

        @ApiParam(name = "Project File", value = "File containing the project page tab definition")
        @RequestParam(PROJECT) file: MultipartFile
    ): Submission = projectWebHandler.submit(user, tmpFileGenerator.asFile(file))

    @PostMapping(
        value = ["/{accNo}/submissions"],
        headers = ["${HttpHeaders.CONTENT_TYPE}=$MULTIPART_FORM_DATA"],
        produces = [APPLICATION_JSON_VALUE])
    @ResponseBody
    @ApiOperation("Attaches the submission to the project with the given accession")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun attachSubmission(
        @BioUser user: SecurityUser,

        @ApiParam(name = "AccNo", value = "Parent project accession")
        @PathVariable accNo: String,

        @ApiParam(name = "Submission File", value = "File containing the submission page tab definition")
        @RequestParam(SUBMISSION) file: MultipartFile,

        @ApiParam(name = "Files", value = "Files included in the submission")
        @RequestParam(FILES) files: Array<MultipartFile>
    ): Submission =
        submissionWebHandler.submit(user, tempFileGenerator.asFile(file), tempFileGenerator.asFiles(files), accNo)
}
