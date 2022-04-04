package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup

class Md5QueryHandler(
    private val fireDB: FireMockDatabase
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.GET
    override val urlPattern: Regex = "/fire/objects/md5/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val md5 = urlPattern.getGroup(rqt.url, 1)
        val fireObject = fireDB.findByMd5(md5)
        return ResponseDefinition.okForJson(fireObject)
    }
}

class PathQueryHandler(
    private val fireDB: FireMockDatabase
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.GET
    override val urlPattern: Regex = "/fire/objects/path/(.*)".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val path = urlPattern.getGroup(rqt.url, 1)
        val fireObject = fireDB.findByPath(path)
        return ResponseDefinition.okForJson(fireObject)
    }
}
