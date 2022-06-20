package ac.uk.ebi.biostd.itest.wiremock

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.itest.wiremock.handlers.DownloadHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.FileSaveHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.Md5QueryHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.PathDownloadHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.PathQueryHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.PublishHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.QueryMetadataHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.RequestHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.SaveMetadataHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.SetPathHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.UnPublishHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.UnSetPathHandler
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
        request: Request,
        responseDefinition: ResponseDefinition?,
        files: FileSource?,
        parameters: Parameters?,
    ): ResponseDefinition {
        failIfApply()
        return handlers
            .firstOrNull { it.urlPattern.matches(request.url) && it.requestMethod == request.method }
            ?.handle(request)
            ?: throw WebClientException(HttpStatus.BAD_REQUEST, "http method ${request.method.name} is not supported")
    }

    private fun failIfApply() {
        if (failFactor == null) return
        if (Random.nextInt(0, failFactor) == 0) throw WebClientException(INTERNAL_SERVER_ERROR, "Simulated Error")
    }

    companion object {
        const val name = "testWireMockTransformer"

        fun newTransformer(
            subFolder: Path,
            ftpFolder: Path,
            dbFolder: Path,
            failFactor: Int?,
        ): TestWireMockTransformer {
            val fireDatabase = FireMockDatabase(subFolder, ftpFolder, dbFolder)

            return TestWireMockTransformer(
                failFactor,
                listOf(
                    Md5QueryHandler(fireDatabase),
                    PathQueryHandler(fireDatabase),
                    QueryMetadataHandler(fireDatabase),
                    SaveMetadataHandler(fireDatabase),
                    FileSaveHandler(fireDatabase),
                    PathDownloadHandler(fireDatabase),
                    SetPathHandler(fireDatabase),
                    UnSetPathHandler(fireDatabase),
                    PublishHandler(fireDatabase),
                    UnPublishHandler(fireDatabase),
                    DownloadHandler(fireDatabase)
                )
            )
        }
    }
}
