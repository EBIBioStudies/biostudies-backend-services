package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup

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

class PathQueryHandler(
    private val fireDB: FireMockDatabase,
) : RequestHandler {
    override val method: RequestMethod = RequestMethod.GET
    override val urlPattern: Regex = "$FIRE_BASE_URL/path/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val path = urlPattern.getGroup(rqt.url, 1)
        val fireObject = fireDB.findByPath(path)

        return if (fireObject == null) ResponseDefinition.notFound() else ResponseDefinition.okForJson(fireObject)
    }
}
