package uk.ac.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import uk.ac.ebi.biostd.base.nullIfBlank
import uk.ac.ebi.biostd.base.orFalse
import uk.ac.ebi.biostd.common.NAME
import uk.ac.ebi.biostd.common.NAME_ATTRS
import uk.ac.ebi.biostd.common.REFERENCE
import uk.ac.ebi.biostd.common.VALUE
import uk.ac.ebi.biostd.common.VAL_ATTRS
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.findNode
import uk.ac.ebi.biostd.serialization.extensions.getNode
import uk.ac.ebi.biostd.validation.InvalidElementException
import uk.ac.ebi.biostd.validation.REQUIRED_ATTR_NAME

internal class AttributeJsonDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val name = node.getNode<TextNode>(NAME).textValue()
        val value = node.findNode<TextNode>(VALUE)?.textValue().nullIfBlank()
        require(name.isNotBlank()) { throw InvalidElementException(REQUIRED_ATTR_NAME) }

        return Attribute(
            name = name,
            value = value,
            reference = node.get(REFERENCE)?.asBoolean().orFalse(),
            nameAttrs = mapper.convertOrDefault(node, NAME_ATTRS) { mutableListOf() },
            valueAttrs = mapper.convertOrDefault(node, VAL_ATTRS) { mutableListOf() },
        )
    }
}
