package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.multipartBody
import ac.uk.ebi.biostd.client.extensions.deserializeResponse
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ATTRIBUTES
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.io.File

typealias SubmissionResponse = ClientResponse<Submission>

private const val SUBMIT_URL = "/submissions"

internal class MultiPartSubmitClient(
    private val client: WebClient,
    private val serializationService: SerializationService,
) : MultipartSubmitOperations {
    override fun submitSingle(
        submission: File,
        filesConfig: SubmissionFilesConfig,
        attrs: Map<String, String>,
    ): SubmissionResponse {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val multiPartBody = multipartBody(filesConfig, FileSystemResource(submission)).apply {
            attrs.entries.forEach { add(ATTRIBUTES, ExtAttributeDetail(it.key, it.value)) }
        }

        val response = client.post()
            .uri("$SUBMIT_URL/direct")
            .body(BodyInserters.fromMultipartData(multiPartBody))
            .headers { it.addAll(headers) }
            .retrieve()

        return serializationService.deserializeResponse(response, JsonPretty).block()!!
    }

    override fun submitSingle(
        submission: String,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): SubmissionResponse {
        val headers = createHeaders(format)
        val body = multipartBody(filesConfig, submission)

        return submit(headers, body)
    }

    override fun submitSingle(
        submission: Submission,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): SubmissionResponse {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val body = multipartBody(filesConfig, serializedSubmission)

        return submit(headers, body)
    }

    private fun submit(
        headers: HttpHeaders,
        body: LinkedMultiValueMap<String, Any>,
        url: String = SUBMIT_URL,
    ): SubmissionResponse {
        val response = client.post()
            .uri(url)
            .body(BodyInserters.fromMultipartData(body))
            .headers { it.addAll(headers) }
            .retrieve()

        return serializationService.deserializeResponse(response, JsonPretty).block()!!
    }

    private fun createHeaders(format: SubmissionFormat) = HttpHeaders().apply {
        contentType = MediaType.MULTIPART_FORM_DATA
        accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        setSubmissionType(format.submissionType)
    }
}
