package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.multipartBody
import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.MultipartAsyncSubmitOperations
import ac.uk.ebi.biostd.client.integration.web.SubmissionFilesConfig
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ATTRIBUTES
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import java.io.File

private const val SUBMIT_URL = "/submissions/async"

class MultiPartAsyncSubmitClient(
    private val client: WebClient,
    private val serializationService: SerializationService,
) : MultipartAsyncSubmitOperations {
    override fun asyncSubmitSingle(
        submission: File,
        filesConfig: SubmissionFilesConfig,
        attrs: Map<String, String>,
    ): AcceptedSubmission {
        val headers = httpHeadersOf(HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA)
        val multiPartBody =
            multipartBody(
                filesConfig,
                FileSystemResource(submission),
                attrs.entries.map { ATTRIBUTES to ExtAttributeDetail(it.key, it.value) },
            )
        return client.postForObject("$SUBMIT_URL/direct", RequestParams(headers, multiPartBody))
    }

    override fun asyncSubmitSingle(
        submission: String,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): AcceptedSubmission {
        val headers = createHeaders(format)
        val body = multipartBody(filesConfig, submission)
        return client.postForObject(SUBMIT_URL, RequestParams(headers, body))
    }

    override fun asyncSubmitSingle(
        submission: Submission,
        format: SubmissionFormat,
        filesConfig: SubmissionFilesConfig,
    ): AcceptedSubmission {
        val headers = createHeaders(format)
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val body = multipartBody(filesConfig, serializedSubmission)
        return client.postForObject(SUBMIT_URL, RequestParams(headers, body))
    }

    private fun createHeaders(format: SubmissionFormat): HttpHeaders =
        HttpHeaders().apply {
            contentType = MediaType.MULTIPART_FORM_DATA
            accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
            setSubmissionType(format.submissionType)
        }
}
