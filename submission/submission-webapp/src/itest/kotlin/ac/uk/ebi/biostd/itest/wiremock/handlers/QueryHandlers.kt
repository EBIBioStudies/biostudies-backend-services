package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import uk.ac.ebi.fire.client.model.MetadataEntry

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
        return if (fireObject == null) ResponseDefinition.notFound() else ResponseDefinition.okForJson(fireObject)
    }
}

class QueryMetadataHandler(
    private val objectMapper: ObjectMapper = ObjectMapper(),
    private val fireDB: FireMockDatabase
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.POST
    override val urlPattern: Regex = "/fire/objects/metadata".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val keys = objectMapper.readValue<Map<String, String>>(rqt.body).map { MetadataEntry(it.key, it.value) }
        val matches = fireDB.findByMetadata(keys)
        return ResponseDefinition.okForJson(matches)
    }
}
