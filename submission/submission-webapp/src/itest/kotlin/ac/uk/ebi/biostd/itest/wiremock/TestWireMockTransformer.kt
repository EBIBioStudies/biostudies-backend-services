package ac.uk.ebi.biostd.itest.wiremock

import ac.uk.ebi.biostd.client.exception.WebClientException
import ac.uk.ebi.biostd.itest.wiremock.handlers.FileSaveHandler
import ac.uk.ebi.biostd.itest.wiremock.handlers.Md5QueryHandler
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
import java.nio.file.Path

class TestWireMockTransformer private constructor(
    private val db: FireMockDatabase,
    private val handlers: List<RequestHandler>
) :
    ResponseDefinitionTransformer() {
    override fun getName(): String = "testWireMockTransformer"

    override fun transform(
        request: Request,
        responseDefinition: ResponseDefinition?,
        files: FileSource?,
        parameters: Parameters?
    ): ResponseDefinition {
        return handlers
            .firstOrNull { it.urlPattern.matches(request.url) && it.requestMethod == request.method }
            ?.handle(request)
            ?: throw WebClientException(HttpStatus.BAD_REQUEST, "http method ${request.method.name} is not supported")
    }

    fun cleanDb() = db.cleanAll()

    companion object {
        fun newTransformer(
            subFolder: Path,
            ftpFolder: Path,
            dbFolder: Path,
        ): TestWireMockTransformer {
            val fireDatabase = FireMockDatabase(subFolder, ftpFolder, dbFolder)
            return TestWireMockTransformer(
                fireDatabase,
                listOf(
                    Md5QueryHandler(fireDB = fireDatabase),
                    PathQueryHandler(fireDB = fireDatabase),
                    QueryMetadataHandler(fireDB = fireDatabase),
                    SaveMetadataHandler(fireDB = fireDatabase),
                    FileSaveHandler(fireDB = fireDatabase),
                    SetPathHandler(fireDB = fireDatabase),
                    UnSetPathHandler(fireDB = fireDatabase),
                    PublishHandler(fireDB = fireDatabase),
                    UnPublishHandler(fireDB = fireDatabase),
                )
            )
        }
    }
}
