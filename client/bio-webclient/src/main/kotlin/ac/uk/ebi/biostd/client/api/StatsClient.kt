package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.extensions.httpHeadersOf
import ac.uk.ebi.biostd.client.extensions.linkedMultiValueMapOf
import ac.uk.ebi.biostd.client.integration.web.StatsOperations
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.commons.http.ext.retrieveBlocking
import ebi.ac.uk.model.SubmissionStat
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.io.File

private const val STATS_URL = "/stats"

class StatsClient(
    private val client: WebClient,
) : StatsOperations {
    override fun getStatsByAccNo(accNo: String): List<SubmissionStat> {
        return client.getForObject<Array<SubmissionStat>>("$STATS_URL/submission/$accNo").toList()
    }

    override fun getStatsByType(type: String): List<SubmissionStat> {
        return client.getForObject<Array<SubmissionStat>>("$STATS_URL/$type").toList()
    }

    override fun getStatsByTypeAndAccNo(type: String, accNo: String): SubmissionStat {
        return client.getForObject<SubmissionStat>("$STATS_URL/$type/$accNo")
    }

    override fun registerStat(stat: SubmissionStat) {
        return client.postForObject(STATS_URL, RequestParams(body = stat))
    }

    override fun registerStats(type: String, statsFile: File): List<SubmissionStat> {
        val headers = httpHeadersOf(
            HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE
        )
        val multiPartBody = linkedMultiValueMapOf("stats" to FileSystemResource(statsFile))
        return client.post()
            .uri("$STATS_URL/$type")
            .body(BodyInserters.fromMultipartData(multiPartBody))
            .headers { it.addAll(headers) }
            .retrieveBlocking()!!
    }

    override fun incrementStats(type: String, statsFile: File): List<SubmissionStat> {
        val headers = httpHeadersOf(
            HttpHeaders.CONTENT_TYPE to MediaType.MULTIPART_FORM_DATA,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE
        )
        val multiPartBody = linkedMultiValueMapOf("stats" to FileSystemResource(statsFile))
        return client.post()
            .uri("$STATS_URL/$type/increment")
            .body(BodyInserters.fromMultipartData(multiPartBody))
            .headers { it.addAll(headers) }
            .retrieveBlocking()!!
    }
}
