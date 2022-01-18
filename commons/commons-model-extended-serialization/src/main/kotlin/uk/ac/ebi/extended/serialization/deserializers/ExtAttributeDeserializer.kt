package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.extended.model.ExtAttribute
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME_ATTRS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_REFERENCE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VALUE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VAL_ATTRS
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode

class ExtAttributeDeserializer : JsonDeserializer<ExtAttribute>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtAttribute {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)

        return ExtAttribute(
            name = node.getNode<TextNode>(ATTR_NAME).textValue(),
            value = node.findNode<TextNode>(ATTR_VALUE)?.textValue().nullIfBlank(),
            reference = node.get(ATTR_REFERENCE)?.asBoolean().orFalse(),
            nameAttrs = mapper.convertOrDefault(node, ATTR_NAME_ATTRS) { mutableListOf() },
            valueAttrs = mapper.convertOrDefault(node, ATTR_VAL_ATTRS) { mutableListOf() },
        )
    }
}
