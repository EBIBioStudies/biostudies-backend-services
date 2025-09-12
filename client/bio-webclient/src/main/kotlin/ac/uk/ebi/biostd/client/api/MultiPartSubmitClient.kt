package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.multipartBody
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartSubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.model.Submission
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
    override suspend fun submitMultipart(
        sub: File,
        parameters: SubmitParameters,
        files: List<File>,
    ): SubmissionResponse {
        val headers = httpHeadersOf(HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA)
        val body =
            multipartBody(
                submission = FileSystemResource(sub),
                parameters = parameters,
                files = files,
            )
        return submit("/submissions/direct", headers, body)
    }

    override suspend fun submitMultipart(
        sub: String,
        format: SubmissionFormat,
        parameters: SubmitParameters,
        files: List<File>,
    ): SubmissionResponse {
        val headers = createHeaders(format)
        val body = multipartBody(sub, parameters, files)
        return submit("/submissions", headers, body)
    }

    override suspend fun submitMultipart(
        sub: Submission,
        format: SubmissionFormat,
        parameters: SubmitParameters,
        files: List<File>,
    ): SubmissionResponse {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(sub, format.asSubFormat())
        val body = multipartBody(serializedSubmission, parameters, files)
        return submit("/submissions", headers, body)
    }

    private suspend fun submit(
        url: String,
        headers: HttpHeaders,
        body: LinkedMultiValueMap<String, Any>,
    ): SubmissionResponse {
        val response =
            client
                .post()
                .uri(url)
                .body(BodyInserters.fromMultipartData(body))
                .headers { it.addAll(headers) }
                .retrieve()
                .toEntity<String>()
                .awaitSingle()
        return SubmissionResponse(
            body = serializationService.deserializeSubmission(response.body!!, JsonPretty),
            statusCode = response.statusCode.value(),
        )
    }

    private fun createHeaders(format: SubmissionFormat) =
        HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
            accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
            setSubmissionType(format.submissionType)
        }
}
