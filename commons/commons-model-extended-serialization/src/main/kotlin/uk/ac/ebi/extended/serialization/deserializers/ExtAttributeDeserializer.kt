package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtAttribute
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTR_VALUE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.NAME_ATTRS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.REFERENCE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.VALUE_ATTRS
import uk.ac.ebi.extended.serialization.exception.ExtAttributeNameRequiredException
import uk.ac.ebi.extended.serialization.exception.ExtAttributeValueRequiredException
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode

class ExtAttributeDeserializer : JsonDeserializer<ExtAttribute>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtAttribute {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)
        val name = getName(node)

        return ExtAttribute(
            name,
            getValue(node, name),
            node.getNode<BooleanNode>(REFERENCE).booleanValue(),
            mapper.convertList(node.findNode(NAME_ATTRS)),
            mapper.convertList(node.findNode(VALUE_ATTRS))
        )
    }

    private fun getName(node: JsonNode): String =
        runCatching { node.getNode<TextNode>(ATTR_NAME).textValue() }
            .onFailure { throw ExtAttributeNameRequiredException() }
            .getOrThrow()

    private fun getValue(node: JsonNode, attrName: String): String =
        runCatching { node.getNode<TextNode>(ATTR_VALUE).textValue() }
            .onFailure { throw ExtAttributeValueRequiredException(attrName) }
            .getOrThrow()
}
