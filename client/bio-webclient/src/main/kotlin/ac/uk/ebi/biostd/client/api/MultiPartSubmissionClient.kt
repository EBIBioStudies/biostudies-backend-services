package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.FILES
import ebi.ac.uk.model.constants.SUBMISSION
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.io.File

typealias SubmissionResponse = ClientResponse<Submission>
typealias RequestMap = HttpEntity<LinkedMultiValueMap<String, Any>>

private const val SUBMIT_URL = "/submissions"

internal class MultiPartSubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : MultipartSubmissionOperations {
    override fun submitSingle(submission: File, files: List<File>, attrs: Map<String, String>): SubmissionResponse {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val multiPartBody = getMultipartBody(files, FileSystemResource(submission)).apply {
            attrs.entries.forEach { add(it.key, it.value) }
        }

        return template.postForEntity<String>("$SUBMIT_URL/direct", (HttpEntity(multiPartBody, headers)))
            .map { body -> serializationService.deserializeSubmission(body, JsonPretty) }
            .let { ClientResponse(it.body!!, it.statusCode.value()) }
    }

    override fun submitSingle(submission: String, format: SubmissionFormat, files: List<File>): SubmissionResponse {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, submission)
        return submit(HttpEntity(body, headers))
    }

    override fun submitSingle(submission: Submission, format: SubmissionFormat, files: List<File>): SubmissionResponse {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, serializationService.serializeSubmission(submission, format.asSubFormat()))
        return submit(HttpEntity(body, headers))
    }

    private fun submit(request: RequestMap, url: String = SUBMIT_URL): SubmissionResponse =
        template.postForEntity<String>(url, request)
            .map { serializationService.deserializeSubmission(it, JSON) }
            .let { ClientResponse(it.body!!, it.statusCode.value()) }

    private fun createHeaders(format: SubmissionFormat) = HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        setSubmissionType(format.submissionType)
    }

    private fun getMultipartBody(files: List<File>, submission: Any) =
        LinkedMultiValueMap(
            files.map { FILES to FileSystemResource(it) }
                .plus(SUBMISSION to submission)
                .groupBy({ it.first }, { it.second })
        )
}
