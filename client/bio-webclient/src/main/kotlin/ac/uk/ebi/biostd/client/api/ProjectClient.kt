package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.ProjectOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.PROJECT
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
        val format = SubmissionFormat.fromSubFormat(serializationService.getSubmissionFormat(project))
        val headers = createHeaders(format)
        val body = getMultipartBody(FileSystemResource(project))

        return template
            .postForEntity<String>("/projects", HttpEntity(body, headers))
            .map { serializationService.deserializeSubmission(it, format.asSubFormat()) }
    }

    private fun createHeaders(format: SubmissionFormat) = HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
    }

    private fun getMultipartBody(project: Any) = LinkedMultiValueMap<String, Any>().apply {
        add(PROJECT, project)
    }
}
