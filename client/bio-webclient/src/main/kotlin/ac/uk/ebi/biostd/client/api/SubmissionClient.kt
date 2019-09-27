package ac.uk.ebi.biostd.client.api

import ebi.ac.uk.api.dto.SubmissionDto
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder

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

    override fun deleteSubmission(accNo: String) = template.delete("$SUBMISSIONS_URL/$accNo")

    private fun submitSingle(request: HttpEntity<String>, format: SubmissionFormat): ResponseEntity<Submission> =
        template.postForEntity<String>(SUBMISSIONS_URL, request)
            .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun createHeaders(format: SubmissionFormat): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = format.mediaType
        headers.accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        headers.setSubmissionType(format.mediaType)
        return headers
    }

    override fun getSubmissions(filter: Map<String, Any>): List<SubmissionDto> {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL)
        filter.entries.forEach { builder.queryParam(it.key, it.value) }
        return template.getForObject<Array<SubmissionDto>>(builder.toUriString()).orEmpty().toList()
    }
}
