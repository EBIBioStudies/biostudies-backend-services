package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import java.io.File

class ProjectWebHandler(
    private val projectService: ProjectService,
    private val serializationService: SerializationService
) {
    fun submit(user: SecurityUser, projectFile: File): Submission {
        val project = serializationService.deserializeSubmission(projectFile)
        return projectService.submit(project, user)
    }
}
