package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.PostProcessOperations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

class PostProcessOperationsClient(
    private val client: WebClient,
) : PostProcessOperations {
    override suspend fun postProcess(accNo: String) {
        client
            .post()
            .uri("$EXT_SUBMISSIONS_URL/$accNo/post-process")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun recalculateStats(accNo: String) {
        client
            .post()
            .uri("$EXT_SUBMISSIONS_URL/$accNo/post-process/stats")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun copyPageTab(accNo: String) {
        client
            .post()
            .uri("$EXT_SUBMISSIONS_URL/$accNo/post-process/copy-pagetab")
            .retrieve()
            .awaitBodilessEntity()
    }
}
