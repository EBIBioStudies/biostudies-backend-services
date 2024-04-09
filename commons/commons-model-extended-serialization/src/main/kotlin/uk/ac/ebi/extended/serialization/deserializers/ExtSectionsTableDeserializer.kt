package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.extended.model.ExtSectionTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTIONS
import uk.ac.ebi.serialization.extensions.convertOrDefault

class ExtSectionsTableDeserializer : JsonDeserializer<ExtSectionTable>() {
    override fun deserialize(
        jsonParser: JsonParser,
        ctxt: DeserializationContext,
    ): ExtSectionTable {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)
        return ExtSectionTable(mapper.convertOrDefault(node, SECTIONS) { emptyList() })
    }
}
