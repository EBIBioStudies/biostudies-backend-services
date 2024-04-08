package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.SubmissionOperations
import ebi.ac.uk.api.dto.SubmissionDto
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.delete
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.model.constants.ACC_NO
import ebi.ac.uk.model.constants.FILE_LIST_NAME
import ebi.ac.uk.model.constants.ROOT_PATH
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

internal const val SUBMISSIONS_URL = "/submissions"

internal class SubmissionClient(
    private val client: WebClient,
) : SubmissionOperations {
    override fun deleteSubmission(accNo: String) {
        client.delete("$SUBMISSIONS_URL/$accNo")
    }

    override fun deleteSubmissions(submissions: List<String>) {
        client.delete("$SUBMISSIONS_URL?submissions=${submissions.joinToString(",")}")
    }

    override fun getSubmissions(filter: Map<String, Any>): List<SubmissionDto> {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSIONS_URL)
        filter.entries.forEach { builder.queryParam(it.key, it.value) }

        return client.getForObject<Array<SubmissionDto>>(builder.toUriString()).toList()
    }

    override fun validateFileList(
        fileListPath: String,
        rootPath: String?,
        accNo: String?,
    ) {
        val headers = HttpHeaders().apply { contentType = APPLICATION_FORM_URLENCODED }
        val formData =
            buildList {
                add(FILE_LIST_NAME to fileListPath)
                rootPath?.let { add(ROOT_PATH to it) }
                accNo?.let { add(ACC_NO to it) }
            }
        val body = LinkedMultiValueMap(formData.groupBy({ it.first }, { it.second }))

        client.post("$SUBMISSIONS_URL/fileLists/validate", RequestParams(headers, body))
    }
}
