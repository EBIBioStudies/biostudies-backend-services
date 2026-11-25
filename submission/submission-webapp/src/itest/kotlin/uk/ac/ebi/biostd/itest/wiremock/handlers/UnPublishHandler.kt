package uk.ac.ebi.biostd.itest.wiremock.handlers

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import uk.ac.ebi.biostd.itest.wiremock.FireMockDatabase
import uk.ac.ebi.biostd.util.regex.getGroup

class UnPublishHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.DELETE
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)/publish".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val file = fireDB.unpublish(urlPattern.getGroup(rqt.url, 1))
        return ResponseDefinition.okForJson(file)
    }
}
