package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup

class PublishHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.PUT
    override val urlPattern: Regex = "/fire/objects/(.*)/publish".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        fireDB.publish(urlPattern.getGroup(rqt.url, 1))
        return ResponseDefinition.ok()
    }
}