package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.StatsOperations
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.commons.http.builder.linkedMultiValueMapOf
import ebi.ac.uk.model.SubmissionStat
import ebi.ac.uk.model.UpdateResult
import kotlinx.coroutines.flow.Flow
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow
import java.io.File

private const val STATS_URL = "/stats"

class StatsClient(
    private val client: WebClient,
) : StatsOperations {
    override fun findByAccNo(accNo: String): Flow<SubmissionStat> =
        client
            .get()
            .uri("$STATS_URL/submission/$accNo")
            .retrieve()
            .bodyToFlow()

    override fun findByType(type: String): Flow<SubmissionStat> =
        client
            .get()
            .uri("$STATS_URL/$type")
            .retrieve()
            .bodyToFlow()

    override suspend fun findByTypeAndAccNo(
        type: String,
        accNo: String,
    ): SubmissionStat =
        client
            .get()
            .uri("$STATS_URL/$type/$accNo")
            .retrieve()
            .awaitBody()

    override suspend fun incrementStats(
        type: String,
        statsFile: File,
    ): UpdateResult {
        val headers =
            httpHeadersOf(
                HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA,
                HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            )
        val multiPartBody = linkedMultiValueMapOf("stats" to FileSystemResource(statsFile))
        return client
            .post()
            .uri("$STATS_URL/$type/increment")
            .body(BodyInserters.fromMultipartData(multiPartBody))
            .headers { it.addAll(headers) }
            .retrieve()
            .awaitBody()
    }
}
