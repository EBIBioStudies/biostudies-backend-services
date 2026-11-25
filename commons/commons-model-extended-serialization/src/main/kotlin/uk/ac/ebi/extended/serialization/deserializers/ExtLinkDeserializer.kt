package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtLink
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.getNode
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.URL

class ExtLinkDeserializer : JsonDeserializer<ExtLink>() {
    override fun deserialize(
        jsonParser: JsonParser,
        ctxt: DeserializationContext,
    ): ExtLink {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)

        return ExtLink(
            url = node.getNode<TextNode>(URL).textValue(),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES) { emptyList() },
        )
    }
}
