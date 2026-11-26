package uk.ac.ebi.biostd.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault

class ExtFilesTableDeserializer : JsonDeserializer<ExtFileTable>() {
    override fun deserialize(
        jsonParser: JsonParser,
        ctxt: DeserializationContext,
    ): ExtFileTable {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)
        return ExtFileTable(mapper.convertOrDefault(node, FILES) { emptyList() })
    }
}
