package ac.uk.ebi.biostd.client.submission

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmissionClient
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate

private const val SUBMISSIONS_URL = "/submissions"

internal class SubmissionClientImpl(
    private val serializationService: SerializationService,
    private val template: RestTemplate
) : SubmissionClient {

    override fun submitSingle(submission: Submission, format: SubmissionFormat) =
        submitSingle(httpEntity(getBody(submission, format)) { contentType = format.mediaType; accept = listOf(format.mediaType) }, format)

    override fun submitSingle(submission: String, format: SubmissionFormat) =
        submitSingle(httpEntity(submission) { contentType = format.mediaType; accept = listOf(format.mediaType) }, format)

    private fun submitSingle(request: HttpEntity<String>, format: SubmissionFormat) =
        template.exchange(SUBMISSIONS_URL, HttpMethod.POST, request, String::class.java)
            .map { body -> serializationService.deserializeSubmission(body, format.asSubFormat()) }

    private fun httpEntity(body: String, function: HttpHeaders.() -> Unit) = HttpEntity(body, HttpHeaders().apply(function))

    private fun getBody(submission: Submission, format: SubmissionFormat): String {
        return when (format) {
            SubmissionFormat.JSON -> serializationService.serializeSubmission(submission, SubFormat.JSON)
            SubmissionFormat.TSV -> serializationService.serializeSubmission(submission, SubFormat.TSV)
            SubmissionFormat.XML -> serializationService.serializeSubmission(submission, SubFormat.XML)
        }
    }
}
