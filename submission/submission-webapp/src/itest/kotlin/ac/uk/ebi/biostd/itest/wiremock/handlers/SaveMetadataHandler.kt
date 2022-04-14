package ac.uk.ebi.biostd.itest.wiremock.handlers

import ac.uk.ebi.biostd.itest.wiremock.FireMockDatabase
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestMethod
import com.github.tomakehurst.wiremock.http.ResponseDefinition
import ebi.ac.uk.util.regex.getGroup
import uk.ac.ebi.fire.client.model.MetadataEntry

class SaveMetadataHandler(
    private val fireDB: FireMockDatabase,
    private val objectMapper: ObjectMapper = ObjectMapper()
) : RequestHandler {
    override val requestMethod: RequestMethod = RequestMethod.PUT
    override val urlPattern: Regex = "/fire/objects/(.*)/metadata/set".toRegex()

    override fun handle(rqt: Request): ResponseDefinition {
        val fireId = urlPattern.getGroup(rqt.url, 1)
        val metadata = objectMapper.readValue<Map<String, String>>(rqt.body).map { MetadataEntry(it.key, it.value) }
        fireDB.updateMetadata(fireId, metadata)
        return ResponseDefinition.ok()
    }
}
