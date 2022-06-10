package uk.ac.ebi.fire.client.integration.web

import mu.KotlinLogging
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.listener.RetryListenerSupport
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import uk.ac.ebi.fire.client.api.FireWebClient
import uk.ac.ebi.fire.client.exception.FireWebClientErrorHandler

private const val FIRE_API_BASE = "fire"
private val logger = KotlinLogging.logger {}

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
            .maxAttempts(config.maxAttempts)
            .withListener(LogListener)
            .build()

        private fun createRestTemplate(fireHost: String, fireVersion: String, username: String, password: String) =
            RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory("$fireHost/$FIRE_API_BASE/$fireVersion")
                errorHandler = FireWebClientErrorHandler()
                clientHttpRequestInitializers.add(FireAuthRequestInitializer(username, password))
                requestFactory = SimpleClientHttpRequestFactory().apply {
                    setReadTimeout(0)
                    setConnectTimeout(0)
                    setBufferRequestBody(false)
                }
            }
    }
}

private object LogListener : RetryListenerSupport() {
    override fun <T : Any, E : Throwable> onError(
        ctx: RetryContext,
        callback: RetryCallback<T, E>,
        error: Throwable,
    ) {
        logger.error {
            "Retryable method ${ctx.getAttribute("context.name")}. Attempt ${ctx.retryCount} throw exception $error"
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
