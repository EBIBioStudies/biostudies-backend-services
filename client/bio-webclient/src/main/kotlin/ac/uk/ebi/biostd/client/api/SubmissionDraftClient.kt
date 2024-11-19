package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.DraftSubmissionOperations
import ebi.ac.uk.model.WebSubmissionDraft
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder

private const val SUBMISSION_DRAFT_URL = "/submissions/drafts"

class SubmissionDraftClient(
    private val client: WebClient,
) : DraftSubmissionOperations {
    override suspend fun createSubmissionDraft(content: String): WebSubmissionDraft =
        client
            .post()
            .uri(SUBMISSION_DRAFT_URL)
            .bodyValue(content)
            .retrieve()
            .bodyToMono<WebSubmissionDraft>()
            .awaitSingle()

    override suspend fun getSubmissionDraft(accNo: String): WebSubmissionDraft =
        client
            .get()
            .uri("$SUBMISSION_DRAFT_URL/$accNo")
            .retrieve()
            .bodyToMono<WebSubmissionDraft>()
            .awaitSingle()

    override suspend fun getAllSubmissionDrafts(
        limit: Int,
        offset: Int,
    ): List<WebSubmissionDraft> =
        client
            .get()
            .uri(buildDraftsUrl(limit, offset))
            .retrieve()
            .bodyToMono<Array<WebSubmissionDraft>>()
            .awaitSingle()
            .toList()

    override suspend fun deleteSubmissionDraft(accNo: String) {
        client
            .delete()
            .uri("$SUBMISSION_DRAFT_URL/$accNo")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun updateSubmissionDraft(
        accNo: String,
        content: String,
    ) {
        client
            .put()
            .uri("$SUBMISSION_DRAFT_URL/$accNo")
            .bodyValue(content)
            .retrieve()
            .bodyToMono<WebSubmissionDraft>()
            .awaitSingle()
    }

    private fun buildDraftsUrl(
        limit: Int,
        offset: Int,
    ) = UriComponentsBuilder
        .fromUriString(SUBMISSION_DRAFT_URL)
        .apply {
            queryParam("limit", limit)
            queryParam("offset", offset)
        }.toUriString()
}
