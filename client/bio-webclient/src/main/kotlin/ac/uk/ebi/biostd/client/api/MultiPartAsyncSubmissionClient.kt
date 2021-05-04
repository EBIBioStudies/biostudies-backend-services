package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
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

private const val SUBMIT_URL = "/submissions/async"

class MultiPartAsyncSubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : MultipartAsyncSubmissionOperations {
    override fun asyncSubmitSingle(submission: File, files: List<File>, attrs: Map<String, String>) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val multiPartBody = getMultipartBody(files, FileSystemResource(submission)).apply {
            attrs.entries.forEach { add(it.key, it.value) }
        }

        template.postForEntity<String>("$SUBMIT_URL/direct", (HttpEntity(multiPartBody, headers)))
    }

    override fun asyncSubmitSingle(submission: String, format: SubmissionFormat, files: List<File>) {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, submission)
        return submitAsync(HttpEntity(body, headers))
    }

    override fun asyncSubmitSingle(submission: Submission, format: SubmissionFormat, files: List<File>) {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, serializationService.serializeSubmission(submission, format.asSubFormat()))
        return submitAsync(HttpEntity(body, headers))
    }

    private fun submitAsync(request: RequestMap, url: String = SUBMIT_URL): Unit =
        template.postForEntity<String>(url, request)
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
