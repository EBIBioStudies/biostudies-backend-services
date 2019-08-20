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

private const val SUBMIT_URL = "/submissions"

internal class MultiPartSubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : MultipartSubmissionOperations {
    override fun submitSingle(
        submission: String,
        format: SubmissionFormat,
        files: List<File>
    ): ResponseEntity<Submission> {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, submission)
        return submitSingle(HttpEntity(body, headers), format)
    }

    override fun submitSingle(
        submission: Submission,
        format: SubmissionFormat,
        files: List<File>
    ): ResponseEntity<Submission> {
        val headers = createHeaders(format)
        val body = getMultipartBody(files, serializationService.serializeSubmission(submission, format.asSubFormat()))
        return submitSingle(HttpEntity(body, headers), format)
    }

    override fun submitXlsx(submission: File, files: List<File>): ResponseEntity<Submission> {
        val format = SubmissionFormat.XLSX
        val headers = createHeaders(format)
        val body = LinkedMultiValueMap<String, Any>().apply {
            files.forEach { add(FILES, FileSystemResource(it)) }
            add(SUBMISSION, FileSystemResource(submission))
        }

        return submitSingle(HttpEntity(body, headers), format)
    }

    private fun submitSingle(request: HttpEntity<LinkedMultiValueMap<String, Any>>, format: SubmissionFormat) =
        template
            .postForEntity<String>(SUBMIT_URL, request)
            .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun createHeaders(format: SubmissionFormat) = HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        setSubmissionType(format.submissionType)
    }

    private fun getMultipartBody(files: List<File>, submission: String) = LinkedMultiValueMap<String, Any>().apply {
        files.forEach { add(FILES, FileSystemResource(it)) }
        add(SUBMISSION, submission)
    }
}
