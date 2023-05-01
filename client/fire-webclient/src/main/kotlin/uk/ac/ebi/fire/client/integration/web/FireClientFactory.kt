package uk.ac.ebi.fire.client.integration.web

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import uk.ac.ebi.fire.client.api.FireWebClient
import uk.ac.ebi.fire.client.api.S3Client

private const val FIRE_API_BASE = "fire"

class FireClientFactory private constructor() {
    companion object {
        fun create(
            tmpDirPath: String,
            fireConfig: FireConfig,
            s3Config: S3Config,
            retryConfig: RetryConfig,
        ): FireClient =
            RetryWebClient(
                createHttpClient(tmpDirPath, fireConfig),
                createS3Client(s3Config),
                createRetryTemplate(retryConfig)
            )

        fun amazonS3Client(s3Config: S3Config): AmazonS3 {
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

        private fun createHttpClient(tmpDirPath: String, config: FireConfig): FireWebClient {
            val restTemplate = createRestTemplate(config.fireHost, config.fireVersion, config.username, config.password)
            return FireWebClient(restTemplate)
        }

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
