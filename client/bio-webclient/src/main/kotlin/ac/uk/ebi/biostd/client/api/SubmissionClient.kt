package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ReleaseRequestDto
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.extensions.setSubmissionType
import ac.uk.ebi.biostd.client.integration.commons.SubmissionFormat
import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.api.ClientResponse
import ebi.ac.uk.api.ON_BEHALF_PARAM
import ebi.ac.uk.api.REGISTER_PARAM
import ebi.ac.uk.api.USER_NAME_PARAM
import ebi.ac.uk.api.dto.NonRegistration
import ebi.ac.uk.api.dto.RegisterConfig
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.api.dto.UserRegistration
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.model.constants.PREVIOUS_VERSION_ACC_NO
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder

private const val SUBMISSIONS_URL = "/submissions"

@Suppress("TooManyFunctions")
internal class SubmissionClient(
    private val template: RestTemplate,
    private val serializationService: SerializationService
) : SubmissionOperations {

    override fun submitSingle(submission: Submission, format: SubmissionFormat, register: RegisterConfig):
        SubmissionResponse = submitSingle(HttpEntity(asString(submission, format), createHeaders(format)), register)

    override fun submitSingle(submission: String, format: SubmissionFormat, register: RegisterConfig):
        SubmissionResponse = submitSingle(HttpEntity(submission, createHeaders(format)), register)

    override fun submitSingleFromDraft(draftKey: String) {
        template.postForEntity<Void>("$SUBMISSIONS_URL/drafts/$draftKey/submit")
    }

    override fun submitAsync(submission: String, format: SubmissionFormat, register: RegisterConfig) {
        submitAsyncSingle(HttpEntity(submission, createHeaders(format)), register)
    }

    override fun deleteSubmission(accNo: String) = template.delete("$SUBMISSIONS_URL/$accNo")

    override fun deleteSubmissions(submissions: List<String>) =
        template.delete("$SUBMISSIONS_URL?submissions=${submissions.joinToString(",")}")

    override fun getSubmissions(filter: Map<String, Any>): List<SubmissionDto> {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL)
        filter.entries.forEach { builder.queryParam(it.key, it.value) }
        return template.getForObject<Array<SubmissionDto>>(builder.toUriString()).toList()
    }

    override fun releaseSubmission(request: ReleaseRequestDto) {
        template.put("$SUBMISSIONS_URL/release", request)
    }

    override fun validateFileList(fileListPath: String, previousVersionAccNo: String?) {
        val headers = HttpHeaders().apply { contentType = APPLICATION_FORM_URLENCODED }
        val formData = buildList {
            add(FILE_LIST_NAME to fileListPath)
            if (previousVersionAccNo != null) add(PREVIOUS_VERSION_ACC_NO to previousVersionAccNo)
        }
        val body = LinkedMultiValueMap(formData.groupBy({ it.first }, { it.second }))

        template.postForEntity<Void>("$SUBMISSIONS_URL/fileLists/validate", HttpEntity(body, headers))
    }

    private fun submitSingle(request: HttpEntity<String>, register: RegisterConfig): SubmissionResponse {
        return template
            .postForEntity<String>(buildUrl(register), request)
            .map { body -> serializationService.deserializeSubmission(body, SubFormat.JSON) }
            .let { ClientResponse(it.body!!, it.statusCodeValue) }
    }

    private fun submitAsyncSingle(request: HttpEntity<String>, register: RegisterConfig) {
        template.postForEntity<Void>(buildUrl(register).plus("/async"), request)
    }

    private fun asString(submission: Submission, format: SubmissionFormat) =
        serializationService.serializeSubmission(submission, format.asSubFormat())

    private fun buildUrl(config: RegisterConfig): String {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL)
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

    private fun createHeaders(format: SubmissionFormat): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = format.mediaType
        headers.accept = listOf(format.mediaType, MediaType.APPLICATION_JSON)
        headers.setSubmissionType(format.mediaType)
        return headers
    }
}
