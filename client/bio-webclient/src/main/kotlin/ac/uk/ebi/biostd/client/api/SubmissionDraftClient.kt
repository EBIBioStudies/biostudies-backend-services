package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ebi.ac.uk.model.SubmissionDraft
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import org.springframework.web.util.UriComponentsBuilder

private const val SUBMISSION_DRAFT_URL = "/submissions/drafts"

class SubmissionDraftClient(private val template: RestTemplate) : DraftSubmissionOperations {
    override fun createSubmissionDraft(content: String): SubmissionDraft =
        template.postForObject(SUBMISSION_DRAFT_URL, content)!!

    override fun getSubmissionDraft(accNo: String): SubmissionDraft =
        template.getForObject("$SUBMISSION_DRAFT_URL/$accNo")!!

    override fun getAllSubmissionDrafts(filter: Map<String, Any>): List<SubmissionDraft> {
        val builder = UriComponentsBuilder.fromUriString(SUBMISSION_DRAFT_URL)
        filter.entries.forEach { builder.queryParam(it.key, it.value) }

        return template.getForObject<Array<SubmissionDraft>>(builder.toUriString()).orEmpty().toList()
    }

    override fun deleteSubmissionDraft(accNo: String) = template.delete("$SUBMISSION_DRAFT_URL/$accNo")

    override fun updateSubmissionDraft(accNo: String, content: String): Unit =
        template.put("$SUBMISSION_DRAFT_URL/$accNo", content)
}
