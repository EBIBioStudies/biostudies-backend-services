package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.getMultipartBody
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.model.Submission
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.io.File

private const val SUBMIT_URL = "/submissions/async"

class MultiPartAsyncSubmitClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService,
) : MultipartAsyncSubmitOperations {
    override fun asyncSubmitSingle(
        submission: File,
        filesConfig: SubmissionFilesConfig,
        attrs: Map<String, String>,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val multiPartBody = getMultipartBody(filesConfig, FileSystemResource(submission))
        attrs.entries.forEach { multiPartBody.add(it.key, it.value) }
        template.postForEntity<String>("$SUBMIT_URL/direct", (HttpEntity(multiPartBody, headers)))
    }

    override fun asyncSubmitSingle(
        submission: String,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ) {
        val headers = createHeaders(format)
        val body = getMultipartBody(filesConfig, submission)
        template.postForEntity<String>(SUBMIT_URL, HttpEntity(body, headers))
    }

    override fun asyncSubmitSingle(
        submission: Submission,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ) {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val body = getMultipartBody(filesConfig, serializedSubmission)
        template.postForEntity<String>(SUBMIT_URL, HttpEntity(body, headers))
    }

    private fun createHeaders(format: SubmissionFormat) = HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        setSubmissionType(format.submissionType)
    }
}
