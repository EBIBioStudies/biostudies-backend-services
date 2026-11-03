package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.api.OnBehalfParameters.Companion.ON_BEHALF_PARAM
import ebi.ac.uk.api.OnBehalfParameters.Companion.REGISTER_PARAM
import ebi.ac.uk.api.OnBehalfParameters.Companion.USER_NAME_PARAM
import ebi.ac.uk.api.SubmitParameters
import ebi.ac.uk.api.SubmitParameters.Companion.PREFERRED_SOURCES
import ebi.ac.uk.api.SubmitParameters.Companion.SILENT_MODE
import ebi.ac.uk.api.SubmitParameters.Companion.SINGLE_JOB_MODE
import ebi.ac.uk.api.SubmitParameters.Companion.STORAGE_MODE
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.postForObjectAsync
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.SubmissionId
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

internal class SubmitClient(
    private val client: WebClient,
    private val serializationService: SerializationService,
) : SubmitOperations {
    override suspend fun submit(
        submission: Submission,
        format: SubmissionFormat,
        submitParameters: SubmitParameters?,
        register: OnBehalfParameters?,
    ): SubmissionId {
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        return submit(serializedSubmission, format, submitParameters, register)
    }

    override suspend fun submit(
        submission: String,
        format: SubmissionFormat,
        submitParameters: SubmitParameters?,
        register: OnBehalfParameters?,
    ): SubmissionId {
        val headers = formatHeaders(format)
        val url = buildUrl(register, submitParameters)
        return client.postForObjectAsync(url, RequestParams(headers, submission))
    }

    override suspend fun submitFromDraft(draftKey: String): SubmissionId =
        client
            .post()
            .uri("$SUBMISSIONS_URL/drafts/$draftKey/submit")
            .retrieve()
            .awaitBody()

    private fun buildUrl(
        config: OnBehalfParameters?,
        submitParameters: SubmitParameters?,
    ): String {
        val builder =
            UriComponentsBuilder
                .fromUriString(SUBMISSIONS_URL)
                .optionalQueryParam(STORAGE_MODE, submitParameters?.storageMode)
                .optionalQueryParam(SILENT_MODE, submitParameters?.silentMode)
                .optionalQueryParam(SINGLE_JOB_MODE, submitParameters?.singleJobMode)
                .optionalQueryParam(PREFERRED_SOURCES, submitParameters?.preferredSources?.joinToString())
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
