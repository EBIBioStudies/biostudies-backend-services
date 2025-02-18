package uk.ac.ebi.fire.client.integration.web

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.http.engine.crt.CrtHttpEngine
import aws.smithy.kotlin.runtime.net.url.Url
import ebi.ac.uk.coroutines.RetryConfig
import ebi.ac.uk.coroutines.SuspendRetryTemplate
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.ac.ebi.fire.client.api.FireWebClient
import uk.ac.ebi.fire.client.api.S3KClient

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
                createS3KClient(s3Config),
                SuspendRetryTemplate(retryConfig),
            )

        private fun createS3Client(s3Config: S3Config): S3Client {
            val credentials = StaticCredentialsProvider(Credentials(s3Config.accessKey, s3Config.secretKey))
            return S3Client {
                httpClient = CrtHttpEngine()
                region = s3Config.region
                endpointUrl = Url.parse(s3Config.endpoint)
                forcePathStyle = true
                credentialsProvider = credentials
            }
        }

        private fun createS3KClient(s3Config: S3Config): FireS3Client = S3KClient(s3Config.bucket, createS3Client(s3Config))

        private fun createHttpClient(config: FireConfig): FireWebClient {
            val webClient = createWebClient(config.fireHost, config.fireVersion, config.username, config.password)
            return FireWebClient(webClient)
        }

        private fun createWebClient(
            fireHost: String,
            fireVersion: String,
            username: String,
            password: String,
        ): WebClient {
            val exchangeStrategies =
                ExchangeStrategies
                    .builder()
                    .codecs { configurer ->
                        if (configurer is ClientCodecConfigurer) configurer.defaultCodecs().maxInMemorySize(-1)
                    }.build()

            val httpClient =
                HttpClient.create().doOnConnected { connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(0))
                    connection.addHandlerLast(WriteTimeoutHandler(0))
                }

            return WebClient
                .builder()
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
