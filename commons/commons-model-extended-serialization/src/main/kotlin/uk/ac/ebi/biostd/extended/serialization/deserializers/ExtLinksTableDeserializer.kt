package uk.ac.ebi.biostd.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault

class ExtLinksTableDeserializer : JsonDeserializer<ExtLinkTable>() {
    override fun deserialize(
        jsonParser: JsonParser,
        ctxt: DeserializationContext,
    ): ExtLinkTable {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)
        return ExtLinkTable(mapper.convertOrDefault(node, LINKS) { emptyList() })
    }
}
