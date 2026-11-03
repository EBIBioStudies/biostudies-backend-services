package uk.ac.ebi.biostd.itest.wiremock.handlers

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import uk.ac.ebi.biostd.itest.wiremock.FireMockDatabase

class Md5QueryHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.GET
    override val urlPattern: Regex = "$FIRE_BASE_URL/md5/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val md5 = urlPattern.getGroup(rqt.url, 1)
        val matches = fireDB.findByMd5(md5)

        return ResponseDefinition.okForJson(matches)
    }
}
