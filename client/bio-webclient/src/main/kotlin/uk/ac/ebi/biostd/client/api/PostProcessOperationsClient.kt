package uk.ac.ebi.biostd.client.api

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import uk.ac.ebi.biostd.client.integration.web.PostProcessOperations

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
            .uri("$EXT_SUBMISSIONS_URL/$accNo/post-process/fallback-pagetab")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun indexInnerFiles(accNo: String) {
        client
            .post()
            .uri("$EXT_SUBMISSIONS_URL/$accNo/post-process/inner-files")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun postProcessDoi(accNo: String) {
        client
            .post()
            .uri("$EXT_SUBMISSIONS_URL/$accNo/post-process/doi")
            .retrieve()
            .awaitBodilessEntity()
    }
}
