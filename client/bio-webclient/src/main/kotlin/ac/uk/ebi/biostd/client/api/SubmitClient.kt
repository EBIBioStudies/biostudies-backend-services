package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmitOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.api.ON_BEHALF_PARAM
import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.STORAGE_MODE
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.api.dto.NonRegistration
import ebi.ac.uk.api.dto.RegisterConfig
import ebi.ac.uk.api.dto.UserRegistration
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.Submission
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder

internal class SubmitClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService,
) : SubmitOperations {

    override fun submitSingle(
        submission: Submission,
        format: SubmissionFormat,
        storageMode: StorageMode?,
        register: RegisterConfig,
    ): SubmissionResponse {
        val serializedSubmission = serializationService.serializeSubmission(submission, format.asSubFormat())
        val entity = HttpEntity(serializedSubmission, formatHeaders(format))
        return template
            .postForEntity<String>(buildUrl(register, storageMode), entity)
            .map { body -> serializationService.deserializeSubmission(body, SubFormat.JSON) }
            .let { ClientResponse(it.body!!, it.statusCodeValue) }
    }

    override fun submitSingle(
        submission: String,
        format: SubmissionFormat,
        storageMode: StorageMode?,
        register: RegisterConfig,
    ): SubmissionResponse {
        val entity = HttpEntity(submission, formatHeaders(format))
        return template
            .postForEntity<String>(buildUrl(register, storageMode), entity)
            .map { body -> serializationService.deserializeSubmission(body, SubFormat.JSON) }
            .let { ClientResponse(it.body!!, it.statusCodeValue) }
    }

    override fun submitAsync(
        submission: String,
        format: SubmissionFormat,
        storageMode: StorageMode?,
        register: RegisterConfig,
    ) {
        val headers = formatHeaders(format)
        val url = buildUrl(register, storageMode)
        val entity = HttpEntity(submission, headers)
        template.postForEntity<Void>(url.plus("/async"), entity)
    }

    override fun submitSingleFromDraft(draftKey: String) {
        template.postForEntity<Void>("$SUBMISSIONS_URL/drafts/$draftKey/submit")
    }

    private fun buildUrl(config: RegisterConfig, storageMode: StorageMode?): String {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL)
        return when (config) {
            NonRegistration -> builder.toUriString()
            is UserRegistration ->
                builder
                    .queryParam(REGISTER_PARAM, true)
                    .queryParam(USER_NAME_PARAM, config.name)
                    .queryParam(ON_BEHALF_PARAM, config.email)
                    .optionalQueryParam(STORAGE_MODE, storageMode)
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
