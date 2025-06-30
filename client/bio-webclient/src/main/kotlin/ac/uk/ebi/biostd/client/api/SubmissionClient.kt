package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.model.constants.ACC_NO
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.model.constants.ROOT_PATH
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder

internal const val SUBMISSIONS_URL = "/submissions"

internal class SubmissionClient(
    private val client: WebClient,
) : SubmissionOperations {
    override suspend fun deleteSubmission(accNo: String) {
        client
            .delete()
            .uri("$SUBMISSIONS_URL/$accNo")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun deleteSubmissions(submissions: List<String>) {
        client
            .delete()
            .uri("$SUBMISSIONS_URL?submissions=${submissions.joinToString(",")}")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun getSubmissions(filter: Map<String, Any>): List<SubmissionDto> {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL)
        filter.entries.forEach { builder.queryParam(it.key, it.value) }
        return client
            .get()
            .uri(builder.toUriString())
            .retrieve()
            .awaitBody()
    }

    override suspend fun getSubmission(accNo: String): SubmissionDto? =
        client
            .get()
            .uri("$SUBMISSIONS_URL/$accNo")
            .retrieve()
            .awaitBody()

    override suspend fun getSubmissionJson(accNo: String): String? =
        client
            .get()
            .uri("$SUBMISSIONS_URL/$accNo.json")
            .retrieve()
            .awaitBody()

    override suspend fun validateFileList(
        fileListPath: String,
        rootPath: String?,
        accNo: String?,
    ) {
        val headers = HttpHeaders().apply { contentType = APPLICATION_FORM_URLENCODED }
        var formData =
            buildMap<String, List<String>> {
                put(FILE_LIST_NAME, listOf(fileListPath))
                rootPath?.let { put(ROOT_PATH, listOf(it)) }
                accNo?.let { put(ACC_NO, listOf(it)) }
            }
        client
            .post()
            .uri("$SUBMISSIONS_URL/fileLists/validate")
            .headers { it.addAll(headers) }
            .body(BodyInserters.fromFormData(LinkedMultiValueMap(formData)))
            .retrieve()
            .awaitBodilessEntity()
    }
}
