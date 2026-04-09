package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import mu.KotlinLogging
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

private val logger = KotlinLogging.logger {}

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
        try {
            logger.info { "Started setting path" }
            val fireId = urlPattern.getGroup(rqt.url, 1)
            logger.info { "Setting path: $fireId" }
            val file = fireDB.setPath(fireId, rqt.getHeader("x-fire-path"))
            logger.info { "set path: $file" }

            return ResponseDefinition.okForJson(file)
        } catch (e: Exception) {
            logger.error(e) { "Error setting path" }
        }

        return ResponseDefinition.ok()
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
