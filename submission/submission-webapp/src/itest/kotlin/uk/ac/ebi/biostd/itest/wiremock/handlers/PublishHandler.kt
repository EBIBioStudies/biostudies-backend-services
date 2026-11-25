package uk.ac.ebi.biostd.itest.wiremock.handlers

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import uk.ac.ebi.biostd.itest.wiremock.FireMockDatabase
import uk.ac.ebi.biostd.util.regex.getGroup

class PublishHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.PUT
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)/publish".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val file = fireDB.publish(urlPattern.getGroup(rqt.url, 1))
        return ResponseDefinition.okForJson(file)
    }
}
