package uk.ac.ebi.fire.client.integration.web

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.HttpClient
import uk.ac.ebi.fire.client.api.FireWebClient
import uk.ac.ebi.fire.client.api.S3Client

private const val FIRE_API_BASE = "fire"

class FireClientFactory private constructor() {
    companion object {
        fun create(
            fireConfig: FireConfig,
            s3Config: S3Config,
            retryConfig: RetryConfig,
        ): FireClient =
            RetryWebClient(
                createHttpClient(fireConfig),
                createS3Client(s3Config),
                createRetryTemplate(retryConfig)
            )

        private fun amazonS3Client(s3Config: S3Config): AmazonS3 {
            val basicAWSCredentials = BasicAWSCredentials(s3Config.accessKey, s3Config.secretKey)
            val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(s3Config.endpoint, s3Config.region)
            return AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfiguration)
                .withPathStyleAccessEnabled(true)
                .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
                .build()
        }

        private fun createS3Client(s3Config: S3Config): FireS3Client =
            S3Client(s3Config.bucket, amazonS3Client(s3Config))

        private fun createHttpClient(config: FireConfig): FireWebClient {
            val webClient = createWebClient(config.fireHost, config.fireVersion, config.username, config.password)
            return FireWebClient(webClient)
        }

        private fun createRetryTemplate(config: RetryConfig): RetryTemplate = RetryTemplateBuilder()
            .exponentialBackoff(config.initialInterval, config.multiplier, config.maxInterval)
            .retryOn(listOf(WebClientResponseException::class.java, ResourceAccessException::class.java))
            .maxAttempts(config.maxAttempts)
            .build()

        private fun createWebClient(
            fireHost: String,
            fireVersion: String,
            username: String,
            password: String,
        ): WebClient {
            val exchangeStrategies =
                ExchangeStrategies.builder().codecs { configurer ->
                    if (configurer is ClientCodecConfigurer) configurer.defaultCodecs().maxInMemorySize(-1)
                }.build()

            val httpClient =
                HttpClient.create().doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(0))
                    connection.addHandlerLast(WriteTimeoutHandler(0))
                }

            return WebClient.builder()
                .baseUrl("$fireHost/$FIRE_API_BASE/$fireVersion")
                .defaultHeaders { headers -> headers.setBasicAuth(username, password) }
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(exchangeStrategies)
                .build()
        }
    }
}

data class S3Config(
    val accessKey: String,
    val secretKey: String,
    val region: String,
    val endpoint: String,
    val bucket: String,
)

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
