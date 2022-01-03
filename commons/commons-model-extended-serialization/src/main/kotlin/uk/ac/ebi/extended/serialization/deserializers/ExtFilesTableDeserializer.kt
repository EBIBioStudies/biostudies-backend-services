package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.extended.model.ExtFileTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.serialization.extensions.findNode

class ExtFilesTableDeserializer : JsonDeserializer<ExtFileTable>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtFileTable {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)

        return ExtFileTable(node.findNode<JsonNode>(FILES)?.let { mapper.convertValue(it, FilesType) } ?: emptyList())
    }
}
