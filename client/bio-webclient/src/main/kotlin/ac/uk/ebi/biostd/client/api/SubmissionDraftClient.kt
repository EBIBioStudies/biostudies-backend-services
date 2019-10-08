package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ebi.ac.uk.model.DraftContent
import ebi.ac.uk.model.DraftSubmission
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

private const val SUBMISSION_DRAFT_URL = "/submissions/drafts"

class SubmissionDraftClient(private val template: RestTemplate) : DraftSubmissionOperations {
    override fun getAllSubmissionDrafts(): List<String> =
        template.getForObject<Array<String>>(SUBMISSION_DRAFT_URL).orEmpty().toList()

    override fun createSubmissionDraft(content: String): String =
        template.postForObject(SUBMISSION_DRAFT_URL, content)!!

    override fun getSubmissionDraft(accNo: String): String =
        template.getForObject("$SUBMISSION_DRAFT_URL/$accNo")!!

    override fun searchSubmissionDraft(searchText: String): List<String> =
        template.getForObject<Array<String>>("$SUBMISSION_DRAFT_URL?searchText=$searchText").orEmpty().toList()

    override fun deleteSubmissionDraft(accNo: String) = template.delete("$SUBMISSION_DRAFT_URL/$accNo")

    override fun updateSubmissionDraft(accNo: String, content: String): Unit =
        template.put("$SUBMISSION_DRAFT_URL/$accNo", content)
}
