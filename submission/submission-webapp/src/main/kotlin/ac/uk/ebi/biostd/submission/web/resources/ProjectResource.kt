package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ebi.ac.uk.model.Project
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
@PreAuthorize("isAuthenticated()")
class ProjectResource(private val projectService: ProjectService) {
    @GetMapping
    @ResponseBody
    fun getUserProjects(@BioUser user: SecurityUser): List<Project> = projectService.getAllowedProjects(user, ATTACH)
}
