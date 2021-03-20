package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ebi.ac.uk.model.WebSubmissionDraft
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import org.springframework.web.util.UriComponentsBuilder

private const val SUBMISSION_DRAFT_URL = "/submissions/drafts"

class SubmissionDraftClient(private val template: RestTemplate) : DraftSubmissionOperations {
    override fun createSubmissionDraft(content: String): WebSubmissionDraft =
        template.postForObject(SUBMISSION_DRAFT_URL, content)!!

    override fun getSubmissionDraft(accNo: String): WebSubmissionDraft =
        template.getForObject("$SUBMISSION_DRAFT_URL/$accNo")!!

    override fun getAllSubmissionDrafts(limit: Int, offset: Int): List<WebSubmissionDraft> =
        template.getForObject<Array<WebSubmissionDraft>>(buildDraftsUrl(limit, offset)).orEmpty().toList()

    override fun deleteSubmissionDraft(accNo: String) = template.delete("$SUBMISSION_DRAFT_URL/$accNo")

    override fun updateSubmissionDraft(accNo: String, content: String): Unit =
        template.put("$SUBMISSION_DRAFT_URL/$accNo", content)

    private fun buildDraftsUrl(limit: Int, offset: Int) =
        UriComponentsBuilder.fromUriString(SUBMISSION_DRAFT_URL).apply {
            queryParam("limit", limit)
            queryParam("offset", offset)
        }.toUriString()
}
