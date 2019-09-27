package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.submission.domain.service.ProjectService
import ac.uk.ebi.biostd.submission.domain.service.TempFileGenerator
import ebi.ac.uk.model.Submission
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.multipart.MultipartFile

class ProjectWebHandler(
    private val pageTabReader: PageTabReader,
    private val projectService: ProjectService,
    private val tempFileGenerator: TempFileGenerator,
    private val serializationService: SerializationService
) {
    fun submit(user: SecurityUser, projectFile: MultipartFile): Submission {
        val file = tempFileGenerator.asFile(projectFile)
        val format = serializationService.getSubmissionFormat(file)
        val project = serializationService.deserializeSubmission(pageTabReader.read(file), format)

        return projectService.submit(project, user)
    }
}
