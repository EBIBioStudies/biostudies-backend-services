package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ebi.ac.uk.model.Project
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.annotations.Api
import io.swagger.annotations.ApiImplicitParam
import io.swagger.annotations.ApiOperation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
@PreAuthorize("isAuthenticated()")
@Api(tags = ["Projects"])
class ProjectResource(private val projectService: ProjectService) {
    @GetMapping
    @ResponseBody
    @ApiOperation("Get the list of available projects for the current user")
    @ApiImplicitParam(name = "X-Session-Token", value = "User authentication token", required = true)
    fun getUserProjects(
        @BioUser user: SecurityUser
    ): List<Project> = projectService.getAllowedProjects(user, AccessType.ATTACH)
}
