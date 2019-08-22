package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.FILES
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

typealias SubmissionResponse = ResponseEntity<Submission>
typealias RequestMap = HttpEntity<LinkedMultiValueMap<String, Any>>

private const val SUBMIT_URL = "/submissions"

internal class MultiPartSubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : MultipartSubmissionOperations {

    override fun submitSingle(submission: File, files: List<File>): SubmissionResponse {
        val headers = createHeaders()
        val body = getMultipartBody(files, FileSystemResource(submission))
        return submitSingle(HttpEntity(body, headers), SubmissionFormat.JSON, "$SUBMIT_URL/direct")
    }

    override fun submitSingle(submission: String, format: SubmissionFormat, files: List<File>): SubmissionResponse {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, submission)
        return submitSingle(HttpEntity(body, headers), format)
    }

    override fun submitSingle(submission: Submission, format: SubmissionFormat, files: List<File>): SubmissionResponse {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, serializationService.serializeSubmission(submission, format.asSubFormat()))
        return submitSingle(HttpEntity(body, headers), format)
    }

    private fun submitSingle(request: RequestMap, format: SubmissionFormat, url: String = SUBMIT_URL): SubmissionResponse =
        template.postForEntity<String>(url, request)
            .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun createHeaders(format: SubmissionFormat): HttpHeaders {
        val headers = createHeaders()
        headers.accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        headers.setSubmissionType(format.mediaType)
        return headers
    }

    private fun createHeaders(accept: List<MediaType> = listOf(MediaType.APPLICATION_JSON)): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.accept = accept
        return headers
    }

    private fun getMultipartBody(files: List<File>, submission: Any): LinkedMultiValueMap<String, Any> {
        val map = LinkedMultiValueMap<String, Any>()
        files.forEach { map.add(FILES, FileSystemResource(it)) }
        map.add(SUBMISSION, submission)
        return map
    }
}
