package uk.ac.ebi.fire.client.integration.web

import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import uk.ac.ebi.fire.client.api.FireWebClient

private const val FIRE_API_BASE = "fire"

class FireClientFactory private constructor() {
    companion object {
        fun create(tmpDirPath: String, config: FireConfig): FireClient {
            val restTemplate = createRestTemplate(config.fireHost, config.fireVersion, config.username, config.password)
            return FireWebClient(tmpDirPath, restTemplate)
        }

        fun create(
            tmpDirPath: String,
            fireConfig: FireConfig,
            retryConfig: RetryConfig,
        ): FireClient =
            RetryWebClient(
                create(tmpDirPath, fireConfig),
                createRetryTemplate(retryConfig)
            )

        private fun createRetryTemplate(config: RetryConfig): RetryTemplate = RetryTemplateBuilder()
            .exponentialBackoff(config.initialInterval, config.multiplier, config.maxInterval)
            .retryOn(listOf(HttpServerErrorException::class.java, ResourceAccessException::class.java))
            .maxAttempts(config.maxAttempts)
            .build()

        private fun createRestTemplate(fireHost: String, fireVersion: String, username: String, password: String) =
            RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory("$fireHost/$FIRE_API_BASE/$fireVersion")
                clientHttpRequestInitializers.add(FireAuthRequestInitializer(username, password))
                requestFactory = SimpleClientHttpRequestFactory().apply {
                    setReadTimeout(0)
                    setConnectTimeout(0)
                    setBufferRequestBody(false)
                }
            }
    }
}

data class FireConfig(
    val fireHost: String,
    val fireVersion: String,
    val username: String,
    val password: String,
)

data class RetryConfig(
    val maxAttempts: Int,
    val initialInterval: Long,
    val multiplier: Double,
    val maxInterval: Long,
)
