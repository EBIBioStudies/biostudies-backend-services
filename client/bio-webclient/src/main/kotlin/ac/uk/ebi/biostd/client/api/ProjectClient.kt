package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.web.ProjectOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.PROJECT
import ebi.ac.uk.model.constants.SUBMISSION
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.io.File

class ProjectClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : ProjectOperations {
    override fun submitProject(project: File): ResponseEntity<Submission> {
        val headers = HttpHeaders().apply { MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply {
            add(PROJECT, FileSystemResource(project))
        }

        return template
            .postForEntity<String>("/projects", HttpEntity(body, headers))
            .map { serializationService.deserializeSubmission(it, SubFormat.JSON_PRETTY) }
    }

    override fun attachSubmission(
        projectAccNo: String,
        submission: File,
        files: List<File>
    ): ResponseEntity<Submission> {
        val headers = HttpHeaders().apply { MediaType.MULTIPART_FORM_DATA }
        val body = getMultipartBody(files, FileSystemResource(submission))

        return template
            .postForEntity<String>("/projects/$projectAccNo/submissions", HttpEntity(body, headers))
            .map { serializationService.deserializeSubmission(it, SubFormat.JSON_PRETTY) }
    }

    private fun getMultipartBody(files: List<File>, submission: Any) = LinkedMultiValueMap<String, Any>().apply {
        files.forEach { add(FILES, FileSystemResource(it)) }
        add(SUBMISSION, submission)
    }
}
