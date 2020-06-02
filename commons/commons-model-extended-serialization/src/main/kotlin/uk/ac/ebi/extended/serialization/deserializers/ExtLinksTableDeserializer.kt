package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.extended.model.ExtLinkTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.findNode

class ExtLinksTableDeserializer : JsonDeserializer<ExtLinkTable>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtLinkTable {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)

        return ExtLinkTable(mapper.convertList(node.findNode(LINKS)))
    }
}
