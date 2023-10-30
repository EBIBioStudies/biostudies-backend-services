package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

class FindPathHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.GET
    override val urlPattern: Regex = "$FIRE_BASE_URL/path/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val firePath = URLDecoder.decode(urlPattern.getGroup(rqt.url, 1), UTF_8)
        val file = fireDB.findByPath(firePath)
        return ResponseDefinition.okForJson(file)
    }
}

class SetPathHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.PUT
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)/firePath".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val fireId = urlPattern.getGroup(rqt.url, 1)
        val file = fireDB.setPath(fireId, rqt.getHeader("x-fire-path"))
        return ResponseDefinition.okForJson(file)
    }
}

class UnSetPathHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.DELETE
    override val urlPattern: Regex = "$FIRE_BASE_URL/(.*)/firePath".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val fireId = urlPattern.getGroup(rqt.url, 1)
        fireDB.unsetPath(fireId)
        return ResponseDefinition.ok()
    }
}
