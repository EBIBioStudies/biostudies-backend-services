package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import uk.ac.ebi.fire.client.model.MetadataEntry

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
