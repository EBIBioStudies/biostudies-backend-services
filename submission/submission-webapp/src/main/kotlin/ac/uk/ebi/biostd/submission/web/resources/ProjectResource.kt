package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.web.handlers.ProjectWebHandler
import ebi.ac.uk.model.Project
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.MULTIPART_FORM_DATA
import ebi.ac.uk.model.constants.PROJECT
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.springframework.http.HttpHeaders
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping("/projects")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Projects"])
class ProjectResource(private val projectService: ProjectService, private val projectWebHandler: ProjectWebHandler) {
    @GetMapping
    @ResponseBody
    @ApiOperation("Get the list of available projects for the current user")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun getUserProjects(
        @ApiIgnore
        @AuthenticationPrincipal
        user: SecurityUser
    ): List<Project> = projectService.getAllowedProjects(user, AccessType.ATTACH)

    @PostMapping(headers = ["${HttpHeaders.CONTENT_TYPE}=$MULTIPART_FORM_DATA"])
    @ResponseBody
    @ApiOperation("Register a new project")
    @ApiImplicitParam(name = "X-Session-Token", value = "The authentication token", required = true)
    fun submit(
        @ApiIgnore
        @AuthenticationPrincipal
        user: SecurityUser,

        @ApiParam(name = "Project File", value = "File containing the project page tab definition")
        @RequestParam(PROJECT) file: MultipartFile
    ): Submission = projectWebHandler.submit(user, file)
}
