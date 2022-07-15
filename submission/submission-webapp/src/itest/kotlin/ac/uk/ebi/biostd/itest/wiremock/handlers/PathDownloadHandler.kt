package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import java.io.File

class PathDownloadHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = GET
    override val urlPattern: Regex = "$FIRE_BASE_URL/blob/path/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val file = fireDB.downloadByPath(urlPattern.getGroup(rqt.url, 1))

        return if (file.exists()) buildFileResponse(file) else ResponseDefinition.notFound()
    }

    private fun buildFileResponse(file: File) =
        ResponseDefinitionBuilder
            .responseDefinition()
            .withBody(file.readText())
            .build()
}
