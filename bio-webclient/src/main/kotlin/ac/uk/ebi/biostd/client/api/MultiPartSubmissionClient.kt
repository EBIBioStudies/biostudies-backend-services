package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmissionOperations
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUB_FILES_PARAM
import ebi.ac.uk.model.constants.SUB_PARAM
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.io.File

private const val SUBMIT_URL = "/submissions"

internal class MultiPartSubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : MultipartSubmissionOperations {

    override fun submitSingle(submission: String, format: SubmissionFormat, files: List<File>): ResponseEntity<Submission> {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, submission)
        return submitSingle(HttpEntity(body, headers), format)
    }

    override fun submitSingle(submission: Submission, format: SubmissionFormat, files: List<File>): ResponseEntity<Submission> {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, serializationService.serializeSubmission(submission, format.asSubFormat()))
        return submitSingle(HttpEntity(body, headers), format)
    }

    private fun submitSingle(request: HttpEntity<LinkedMultiValueMap<String, Any>>, format: SubmissionFormat) =
            template.postForEntity(SUBMIT_URL, request, String::class.java)
                    .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun createHeaders(format: SubmissionFormat): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        headers.setSubmissionType(format.mediaType)
        return headers
    }

    private fun getMultipartBody(files: List<File>, submission: String): LinkedMultiValueMap<String, Any> {
        val map = LinkedMultiValueMap<String, Any>()
        files.forEach { map.add(SUB_FILES_PARAM, FileSystemResource(it)) }
        map.add(SUB_PARAM, submission)
        return map
    }
}
