package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate

private const val SUBMISSIONS_URL = "/submissions"

internal class SubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : SubmissionOperations {

    override fun submitSingle(submission: Submission, format: SubmissionFormat) = submitSingle(HttpEntity(
            serializationService.serializeSubmission(submission, format.asSubFormat()),
            createHeaders(format)), format)

    override fun submitSingle(submission: String, format: SubmissionFormat) =
        submitSingle(HttpEntity(submission, createHeaders(format)), format)

    private fun submitSingle(request: HttpEntity<String>, format: SubmissionFormat) =
            template.postForEntity(SUBMISSIONS_URL, request, String::class.java)
                    .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun createHeaders(format: SubmissionFormat): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = format.mediaType
        headers.accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        headers.setSubmissionType(format.mediaType)
        return headers
    }
}
