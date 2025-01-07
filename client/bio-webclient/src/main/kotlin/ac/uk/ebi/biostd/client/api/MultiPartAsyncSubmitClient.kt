package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.multipartBody
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionId
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.File

private const val SUBMIT_URL = "/submissions/async"

class MultiPartAsyncSubmitClient(
    private val client: WebClient,
    private val serializationService: SerializationService,
) : MultipartAsyncSubmitOperations {
    override suspend fun submitMultipartAsync(
        submission: File,
        parameters: SubmitParameters,
    ): SubmissionId {
        val headers = httpHeadersOf(HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA)
        val multiPartBody =
            multipartBody(
                parameters = parameters,
                submission = FileSystemResource(submission),
            )
        return submit("$SUBMIT_URL/direct", headers, multiPartBody)
    }

    override suspend fun submitMultipartAsync(
        submissions: Map<String, String>,
        parameters: SubmitParameters,
        format: String,
        files: Map<String, List<File>>,
    ): List<SubmissionId> {
        val headers =
            httpHeadersOf(
                HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA,
            )

        val multiPartBody =
            multipartBody(
                parameters = parameters,
                submissions = submissions,
                files = files,
                format = format,
            )
        return client
            .post()
            .uri("$SUBMIT_URL/multiple")
            .body(BodyInserters.fromMultipartData(multiPartBody))
            .headers { it.addAll(headers) }
            .retrieve()
            .bodyToMono<List<SubmissionId>>()
            .awaitSingle()
    }

    override suspend fun submitMultipartAsync(
        submission: String,
        format: SubmissionFormat,
        parameters: SubmitParameters,
        files: List<File>,
    ): SubmissionId {
        val headers = createHeaders(format)
        val body = multipartBody(submission, parameters, files)
        return submit(SUBMIT_URL, headers, body)
    }

    override suspend fun submitMultipartAsync(
        submission: Submission,
        format: SubmissionFormat,
        parameters: SubmitParameters,
    ): SubmissionId {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val body = multipartBody(serializedSubmission, parameters)
        return submit(SUBMIT_URL, headers, body)
    }

    private suspend fun submit(
        url: String,
        headers: HttpHeaders,
        body: LinkedMultiValueMap<String, Any>,
    ): SubmissionId =
        client
            .post()
            .uri(url)
            .body(BodyInserters.fromMultipartData(body))
            .headers { it.addAll(headers) }
            .retrieve()
            .bodyToMono<SubmissionId>()
            .awaitSingle()

    private fun createHeaders(format: SubmissionFormat): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
            accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
            setSubmissionType(format.submissionType)
        }
}
