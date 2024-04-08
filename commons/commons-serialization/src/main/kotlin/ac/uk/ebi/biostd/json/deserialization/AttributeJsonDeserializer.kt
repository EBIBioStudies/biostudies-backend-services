package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.NAME_ATTRS
import ac.uk.ebi.biostd.common.REFERENCE
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.common.VAL_ATTRS
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode

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
