package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.model.AccessType
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.SubmissionService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    fun getUserProjects(
        @AuthenticationPrincipal user: SecurityUser) = projectService.getAllowedProjects(user, AccessType.ATTACH)
}
