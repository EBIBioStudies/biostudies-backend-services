package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.delete
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.commons.http.ext.put
import ebi.ac.uk.model.WebSubmissionDraft
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

private const val SUBMISSION_DRAFT_URL = "/submissions/drafts"

class SubmissionDraftClient(
    private val client: WebClient,
) : DraftSubmissionOperations {
    override fun createSubmissionDraft(content: String): WebSubmissionDraft {
        return client.postForObject(SUBMISSION_DRAFT_URL, RequestParams(body = content))
    }

    override fun getSubmissionDraft(accNo: String): WebSubmissionDraft {
        return client.getForObject("$SUBMISSION_DRAFT_URL/$accNo")
    }

    override fun getAllSubmissionDrafts(
        limit: Int,
        offset: Int,
    ): List<WebSubmissionDraft> {
        return client.getForObject<Array<WebSubmissionDraft>>(buildDraftsUrl(limit, offset)).toList()
    }

    override fun deleteSubmissionDraft(accNo: String) {
        client.delete("$SUBMISSION_DRAFT_URL/$accNo")
    }

    override fun updateSubmissionDraft(
        accNo: String,
        content: String,
    ) {
        client.put("$SUBMISSION_DRAFT_URL/$accNo", RequestParams(body = content))
    }

    private fun buildDraftsUrl(
        limit: Int,
        offset: Int,
    ) = UriComponentsBuilder.fromUriString(SUBMISSION_DRAFT_URL).apply {
        queryParam("limit", limit)
        queryParam("offset", offset)
    }.toUriString()
}
