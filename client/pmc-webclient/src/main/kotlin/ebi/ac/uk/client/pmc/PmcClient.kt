package ebi.ac.uk.client.pmc

import com.fasterxml.jackson.annotation.JsonProperty
import ebi.ac.uk.client.pmc.SubmissionResult.ANALISIS_IN_PROGRESS
import ebi.ac.uk.client.pmc.SubmissionResult.PMC_DOES_NOT_EXIST
import ebi.ac.uk.client.pmc.SubmissionResult.UNKNOWN_ERROR
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.awaitExchangeOrNull
import org.springframework.web.reactive.function.client.createExceptionAndAwait
import java.net.URI

enum class SubmissionResult {
    PMC_DOES_NOT_EXIST,
    ANALISIS_IN_PROGRESS,
    UNKNOWN_ERROR,
    OK,
}

private val logger = KotlinLogging.logger {}

class PmcClient(
    private val client: WebClient,
) {
    /**
     * Submits a study for processing via the external PMC text mining API.
     * Bad request is ignored as means other file analisis is currently being executed, and not found means study has
     * been deleted from PMC.
     */
    suspend fun submitStudy(request: PmcAnalisisRequest): SubmissionResult {
        fun asResult(
            response: ClientResponse,
            body: String,
        ): SubmissionResult =
            when {
                body.contains("ft_id not existing in Europe PMC") -> {
                    PMC_DOES_NOT_EXIST
                }

                body.contains("It exists already a submission for this user and ft_id in pending state") -> {
                    ANALISIS_IN_PROGRESS
                }

                else -> {
                    logger.error { "Failed to submit submission statusCode='${response.statusCode()}' body='$body' " }
                    UNKNOWN_ERROR
                }
            }

        return client
            .post()
            .uri("/submit")
            .body(BodyInserters.fromValue(request))
            .awaitExchange { response ->
                when (response.statusCode()) {
                    HttpStatus.BAD_REQUEST -> asResult(response, response.awaitBody<String>())
                    else -> SubmissionResult.OK
                }
            }
    }

    suspend fun getStatus(pmcId: String): PmcAnalisisStatusResponse =
        client
            .get()
            .uri("/getSubmissionStatus/$pmcId")
            .retrieve()
            .awaitBody<PmcAnalisisStatusResponse>()

    suspend fun findStatus(pmcId: String): PmcAnalisisStatusResponse? =
        client
            .get()
            .uri("/getSubmissionStatus/$pmcId")
            .awaitExchangeOrNull { response ->
                when (response.statusCode()) {
                    HttpStatus.NOT_FOUND -> null
                    HttpStatus.OK -> response.awaitBody<PmcAnalisisStatusResponse>()
                    else -> throw response.createExceptionAndAwait()
                }
            }

    suspend fun getResult(
        pmcId: String,
        filename: String,
    ): PmcAnalisisFileResult =
        client
            .get()
            .uri("/getAnnotations/$pmcId/$filename")
            .retrieve()
            .awaitBody<PmcAnalisisFileResult>()

    companion object {
        fun createClient(basicToken: String): PmcClient {
            val client =
                WebClient
                    .builder()
                    .baseUrl("https://www.textminingapi.europepmc.org")
                    .defaultHeader("Authorization", "Basic $basicToken")
                    .build()
            return PmcClient(client)
        }
    }
}

data class PmcAnalisisFileResult(
    @field:JsonProperty("ft_id")
    val pmcId: String,
    val filename: String,
    @field:JsonProperty("anns")
    val results: List<PmcFileLink>,
)

data class PmcFileLink(
    val exact: String,
    val type: String,
    val frecuency: Int,
    val tags: List<PmcFileLinkTag>,
)

data class PmcFileLinkTag(
    val name: String,
    val uri: String,
)

data class PmcAnalisisStatusResponse(
    val status: String,
    val files: List<PmcFileResponse>,
)

data class PmcAnalisisRequest(
    val files: List<PmcFile>,
    val callback: URI,
    @field:JsonProperty("ft_id") val pmcId: String,
)

data class PmcFileResponse(
    val filename: String,
    val status: String,
    val url: String,
)

data class PmcFile(
    val filename: String,
    val url: String,
)
