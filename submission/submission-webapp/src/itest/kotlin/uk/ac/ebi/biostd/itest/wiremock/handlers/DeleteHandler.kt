package uk.ac.ebi.biostd.itest.wiremock.handlers

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import uk.ac.ebi.biostd.itest.wiremock.FireMockDatabase

class DeleteHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.DELETE
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val fireId = urlPattern.getGroup(rqt.url, 1)
        fireDB.delete(fireId)
        return ResponseDefinition.noContent()
    }
}
