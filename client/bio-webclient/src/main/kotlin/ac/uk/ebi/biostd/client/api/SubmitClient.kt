package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.deserializeResponse
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ebi.ac.uk.api.ON_BEHALF_PARAM
import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.STORAGE_MODE
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.api.dto.NonRegistration
import ebi.ac.uk.api.dto.RegisterConfig
import ebi.ac.uk.api.dto.UserRegistration
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

internal class SubmitClient(
    private val client: WebClient,
    private val serializationService: SerializationService,
) : SubmitOperations {

    override fun submitSingle(
        submission: Submission,
        format: SubmissionFormat,
        storageMode: StorageMode?,
        register: RegisterConfig,
    ): SubmissionResponse {
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val response = client.post()
            .uri(buildUrl(register, storageMode))
            .body(BodyInserters.fromValue(serializedSubmission))
            .headers { it.addAll(formatHeaders(format)) }
            .retrieve()

        return serializationService.deserializeResponse(response, JSON).block()!!
    }

    override fun submitSingle(
        submission: String,
        format: SubmissionFormat,
        storageMode: StorageMode?,
        register: RegisterConfig,
    ): SubmissionResponse {
        val response = client.post()
            .uri(buildUrl(register, storageMode))
            .body(BodyInserters.fromValue(submission))
            .headers { it.addAll(formatHeaders(format)) }
            .retrieve()

        return serializationService.deserializeResponse(response, JSON).block()!!
    }

    override fun submitAsync(
        submission: String,
        format: SubmissionFormat,
        storageMode: StorageMode?,
        register: RegisterConfig,
    ) {
        val headers = formatHeaders(format)
        val url = buildUrl(register, storageMode).plus("/async")

        client.post(url, RequestParams(headers, submission))
    }

    override fun submitSingleFromDraftAsync(draftKey: String) {
        client.post("$SUBMISSIONS_URL/drafts/$draftKey/submit")
    }

    override fun submitSingleFromDraft(draftKey: String): SubmissionResponse {
        val response = client.post()
            .uri("$SUBMISSIONS_URL/drafts/$draftKey/submit/sync")
            .retrieve()

        return serializationService.deserializeResponse(response, JSON).block()!!
    }

    private fun buildUrl(config: RegisterConfig, storageMode: StorageMode?): String {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL).optionalQueryParam(STORAGE_MODE, storageMode)
        return when (config) {
            NonRegistration -> builder.toUriString()
            is UserRegistration ->
                builder
                    .queryParam(REGISTER_PARAM, true)
                    .queryParam(USER_NAME_PARAM, config.name)
                    .queryParam(ON_BEHALF_PARAM, config.email)
                    .toUriString()
        }
    }

    private fun formatHeaders(format: SubmissionFormat): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = format.mediaType
        headers.accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        headers.setSubmissionType(format.mediaType)
        return headers
    }
}
