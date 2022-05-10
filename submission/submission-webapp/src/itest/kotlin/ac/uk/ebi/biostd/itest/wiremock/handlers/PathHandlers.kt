package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup

class SetPathHandler(
    private val fireDB: FireMockDatabase
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.PUT
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)/firePath".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val fireId = urlPattern.getGroup(rqt.url, 1)
        fireDB.setPath(fireId, rqt.getHeader("x-fire-path"))
        return ResponseDefinition.ok()
    }
}

class UnSetPathHandler(
    private val fireDB: FireMockDatabase
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.DELETE
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)/firePath".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val fireId = urlPattern.getGroup(rqt.url, 1)
        fireDB.unsetPath(fireId)
        return ResponseDefinition.ok()
    }
}
