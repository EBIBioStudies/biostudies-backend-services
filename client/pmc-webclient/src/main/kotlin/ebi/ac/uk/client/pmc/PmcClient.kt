package ebi.ac.uk.client.pmc

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.net.URI

class PmcClient(
    private val client: WebClient,
) {
    suspend fun submitStudy(request: PmcAnalisisRequest) {
        client
            .post()
            .uri("/submit")
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .awaitBody<String>()
    }

    suspend fun getStatus(pmcId: String): PmcAnalisisStatusResponse {
        return client
            .get()
            .uri("/getSubmissionStatus/$pmcId")
            .retrieve()
            .awaitBody<PmcAnalisisStatusResponse>()
    }

    suspend fun getResult(
        pmcId: String,
        filename: String,
    ): PmcAnalisisFileResult {
        return client
            .get()
            .uri("/getAnnotations/$pmcId/$filename")
            .retrieve()
            .awaitBody<PmcAnalisisFileResult>()
    }

    companion object {
        fun createClient(basicToken: String): PmcClient {
            val client =
                WebClient.builder()
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

data class PmcFileResponse(val filename: String, val status: String, val url: String)

data class PmcFile(val filename: String, val url: String)
