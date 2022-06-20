package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import java.net.HttpURLConnection

class DownloadHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.GET
    override val urlPattern: Regex = "$FIRE_BASE_URL/blob/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val file = fireDB.getFile(urlPattern.getGroup(rqt.url, 1))
        return aResponse().withBody(file.readBytes()).withStatus(HttpURLConnection.HTTP_OK).build()
    }
}
