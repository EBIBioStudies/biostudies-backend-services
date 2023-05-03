package ac.uk.ebi.biostd.itest.wiremock

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.itest.wiremock.handlers.DeleteHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.DownloadHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.FileSaveHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.Md5QueryHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.PublishHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.RequestHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.SetPathHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.UnPublishHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.UnSetPathHandler
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import java.nio.file.Path
import kotlin.random.Random

class TestWireMockTransformer constructor(
    private val failFactor: Int?,
    private val handlers: List<RequestHandler>,
) : ResponseDefinitionTransformer() {
    override fun getName(): String = Companion.name

    override fun transform(
        rqt: Request,
        responseDefinition: ResponseDefinition?,
        files: FileSource?,
        parameters: Parameters?,
    ): ResponseDefinition {
        return failIfApply()
            ?: handlers.firstOrNull { it.urlPattern.matches(rqt.url) && it.method == rqt.method }?.handleSafely(rqt)
            ?: throw WebClientException(HttpStatus.BAD_REQUEST, "http method ${rqt.method.name} is not supported")
    }

    private fun failIfApply(): ResponseDefinition? {
        return when {
            failFactor == null || Random.nextInt(0, failFactor) != 0 -> null
            else -> ResponseDefinition(INTERNAL_SERVER_ERROR.value(), "Simulated Error")
        }
    }

    companion object {
        const val name = "testWireMockTransformer"

        fun newTransformer(
            subFolder: Path,
            ftpFolder: Path,
            dbFolder: Path,
            failFactor: Int?,
            httpEndpoint: String,
            defaultBucket: String,
        ): TestWireMockTransformer {
            val s3System = FireS3Service(defaultBucket, amazonS3Client(httpEndpoint))
            val fileSystem = FireMockFileSystem(dbFolder, ftpFolder, subFolder, s3System)
            val fireDatabase = FireMockDatabase(fileSystem)

            return TestWireMockTransformer(
                failFactor,
                listOf(
                    Md5QueryHandler(fireDatabase),
                    FileSaveHandler(fireDatabase),
                    DeleteHandler(fireDatabase),
                    SetPathHandler(fireDatabase),
                    UnSetPathHandler(fireDatabase),
                    PublishHandler(fireDatabase),
                    UnPublishHandler(fireDatabase),
                    DownloadHandler(fireDatabase)
                )
            )
        }

        private fun amazonS3Client(endpoint: String): AmazonS3 {
            val basicAWSCredentials = BasicAWSCredentials("x", "x")
            val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpoint, "x")
            return AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfiguration)
                .withPathStyleAccessEnabled(true)
                .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
                .build()
        }
    }
}
