package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.AcceptedSubmission
import ac.uk.ebi.biostd.client.extensions.deserializeResponse
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmissionResponse
import ac.uk.ebi.biostd.client.integration.web.SubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.OnBehalfParameters.Companion.ON_BEHALF_PARAM
import ebi.ac.uk.api.OnBehalfParameters.Companion.REGISTER_PARAM
import ebi.ac.uk.api.OnBehalfParameters.Companion.USER_NAME_PARAM
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.api.SubmitParameters.Companion.STORAGE_MODE
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.io.sources.PreferredSource
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
    override fun submit(
        submission: Submission,
        format: SubmissionFormat,
        submitParameters: SubmitParameters?,
        register: OnBehalfParameters?,
    ): SubmissionResponse {
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val response =
            client
                .post()
                .uri(buildUrl(register, submitParameters))
                .body(BodyInserters.fromValue(serializedSubmission))
                .headers { it.addAll(formatHeaders(format)) }
                .retrieve()

        return serializationService.deserializeResponse(response, JSON).block()!!
    }

    override fun submit(
        submission: String,
        format: SubmissionFormat,
        submitParameters: SubmitParameters?,
        register: OnBehalfParameters?,
    ): SubmissionResponse {
        val response =
            client
                .post()
                .uri(buildUrl(register, submitParameters))
                .body(BodyInserters.fromValue(submission))
                .headers { it.addAll(formatHeaders(format)) }
                .retrieve()

        return serializationService.deserializeResponse(response, JSON).block()!!
    }

    override fun submitAsync(
        submission: String,
        format: SubmissionFormat,
        submitParameters: SubmitParameters?,
        register: OnBehalfParameters?,
    ): AcceptedSubmission {
        val headers = formatHeaders(format)
        val url = buildUrl(register, submitParameters).plus("/async")

        return client.postForObject(url, RequestParams(headers, submission))
    }

    override fun submitFromDraftAsync(draftKey: String) {
        client.post("$SUBMISSIONS_URL/drafts/$draftKey/submit")
    }

    override fun submitFromDraft(
        draftKey: String,
        preferredSources: List<PreferredSource>?,
    ): SubmissionResponse {
        val source = preferredSources?.let { "?preferredSources=${it.joinToString()}" }.orEmpty()
        val response =
            client
                .post()
                .uri("$SUBMISSIONS_URL/drafts/$draftKey/submit/sync$source")
                .retrieve()

        return serializationService.deserializeResponse(response, JSON).block()!!
    }

    private fun buildUrl(
        config: OnBehalfParameters?,
        submitParameters: SubmitParameters?,
    ): String {
        val builder =
            UriComponentsBuilder
                .fromUriString(SUBMISSIONS_URL)
                .optionalQueryParam(STORAGE_MODE, submitParameters?.storageMode)
        return when (config) {
            null -> builder.toUriString()
            else ->
                builder
                    .queryParam(REGISTER_PARAM, true)
                    .queryParam(USER_NAME_PARAM, config.userName)
                    .queryParam(ON_BEHALF_PARAM, config.userEmail)
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
