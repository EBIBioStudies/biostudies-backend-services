package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.multipartBody
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ATTRIBUTES
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import java.io.File

typealias SubmissionResponse = ClientResponse<Submission>

internal class MultiPartSubmitClient(
    private val client: WebClient,
    private val serializationService: SerializationService,
) : MultipartSubmitOperations {
    override suspend fun submitSingle(
        sub: File,
        config: SubmissionFilesConfig,
        attrs: Map<String, String>,
    ): SubmissionResponse {
        val headers = httpHeadersOf(HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA)
        val body =
            multipartBody(
                config,
                FileSystemResource(sub),
                attrs.entries.map { ATTRIBUTES to ExtAttributeDetail(it.key, it.value) },
            )
        return submit(headers, body)
    }

    override suspend fun submitSingle(
        sub: String,
        format: SubmissionFormat,
        config: SubmissionFilesConfig,
    ): SubmissionResponse {
        val headers = createHeaders(format)
        val body = multipartBody(config, sub)
        return submit(headers, body)
    }

    override suspend fun submitSingle(
        sub: Submission,
        format: SubmissionFormat,
        config: SubmissionFilesConfig,
    ): SubmissionResponse {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(sub, format.asSubFormat())
        val body = multipartBody(config, serializedSubmission)
        return submit(headers, body)
    }

    private suspend fun submit(
        headers: HttpHeaders,
        body: LinkedMultiValueMap<String, Any>,
        url: String = "/submissions",
    ): SubmissionResponse {
        val response =
            client.post()
                .uri(url)
                .body(BodyInserters.fromMultipartData(body))
                .headers { it.addAll(headers) }
                .retrieve()
                .toEntity<String>()
                .awaitSingle()
        return SubmissionResponse(
            body = serializationService.deserializeSubmission(response.body!!, JsonPretty),
            statusCode = response.statusCodeValue,
        )
    }

    private fun createHeaders(format: SubmissionFormat) =
        HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
            accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
            setSubmissionType(format.submissionType)
        }
}
