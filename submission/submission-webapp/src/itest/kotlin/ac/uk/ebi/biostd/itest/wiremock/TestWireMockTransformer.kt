package ac.uk.ebi.biostd.itest.wiremock

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.itest.wiremock.handlers.DeleteHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.DownloadHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.FileSaveHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.FindPathHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.Md5QueryHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.PublishHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.RequestHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.SetPathHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.UnPublishHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.UnSetPathHandler
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.http.engine.crt.CrtHttpEngine
import aws.smithy.kotlin.runtime.net.url.Url
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import com.github.tomakehurst.wiremock.stubbing.ServeEvent
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import uk.ac.ebi.fire.client.api.S3KClient
import java.nio.file.Path
import kotlin.random.Random

class TestWireMockTransformer(
    private val failFactor: Int?,
    private val fixedDelay: Long,
    private val handlers: List<RequestHandler>,
) : ResponseDefinitionTransformerV2 {
    override fun getName(): String = NAME

    override fun transform(serveEvent: ServeEvent): ResponseDefinition {
        val rqt = serveEvent.request
        Thread.sleep(fixedDelay)
        return failIfApply()
            ?: handlers.firstOrNull { it.urlPattern.matches(rqt.url) && it.method == rqt.method }?.handleSafely(rqt)
            ?: throw WebClientException(HttpStatus.BAD_REQUEST, "http method ${rqt.method.name} is not supported")
    }

    private fun failIfApply(): ResponseDefinition? =
        when {
            failFactor == null || Random.nextInt(0, failFactor) != 0 -> null
            else -> ResponseDefinition(INTERNAL_SERVER_ERROR.value(), "Simulated Error")
        }

    companion object {
        const val NAME = "testWireMockTransformer"

        fun newTransformer(
            subFolder: Path,
            ftpFolder: Path,
            dbFolder: Path,
            failFactor: Int?,
            fixedDelay: Long,
            httpEndpoint: String,
            defaultBucket: String,
        ): TestWireMockTransformer {
            val s3System = FireS3Service(S3KClient(defaultBucket, s3Client(httpEndpoint)))
            val fileSystem = FireMockFileSystem(dbFolder, ftpFolder, subFolder, s3System)
            val fireDatabase = FireMockDatabase(fileSystem)

            return TestWireMockTransformer(
                failFactor,
                fixedDelay,
                listOf(
                    Md5QueryHandler(fireDatabase),
                    FileSaveHandler(fireDatabase),
                    FindPathHandler(fireDatabase),
                    SetPathHandler(fireDatabase),
                    PublishHandler(fireDatabase),
                    UnSetPathHandler(fireDatabase),
                    UnPublishHandler(fireDatabase),
                    DeleteHandler(fireDatabase),
                    DownloadHandler(fireDatabase),
                ),
            )
        }

        private fun s3Client(endpoint: String): S3Client =
            S3Client {
                httpClient = CrtHttpEngine()
                region = "x"
                endpointUrl = Url.parse(endpoint)
                forcePathStyle = true
                credentialsProvider = StaticCredentialsProvider(Credentials("x", "x"))
            }
    }
}
