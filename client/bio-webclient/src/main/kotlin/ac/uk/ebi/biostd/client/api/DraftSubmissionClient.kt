package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ebi.ac.uk.model.DraftSubmission
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject

private const val TEMP_SUBMISSION_URL = "/draft/submissions"

class DraftSubmissionClient(private val template: RestTemplate) : DraftSubmissionOperations {
    override fun getDraftSubmission(accNo: String): DraftSubmission =
        template.getForObject("$TEMP_SUBMISSION_URL/$accNo")!!

    override fun searchDraftSubmission(searchText: String): List<DraftSubmission> =
        template.getForObject<Array<DraftSubmission>>("$TEMP_SUBMISSION_URL?searchText=$searchText").orEmpty().toList()

    override fun deleteDraftSubmission(accNo: String) = template.delete("$TEMP_SUBMISSION_URL/$accNo")

    override fun saveDraftSubmission(accNo: String, content: String): DraftSubmission =
        template.postForObject(TEMP_SUBMISSION_URL, DraftSubmission(accNo, content))!!
}
