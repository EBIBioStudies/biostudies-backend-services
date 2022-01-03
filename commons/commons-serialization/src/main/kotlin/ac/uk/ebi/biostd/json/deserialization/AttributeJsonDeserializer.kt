package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.NAME_ATTRIBUTES
import ac.uk.ebi.biostd.common.REFERENCE
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.common.VAL_ATTRIBUTES
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode

internal object AttributeDetailsType : TypeReference<MutableList<AttributeDetail>>()

internal class AttributeJsonDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val name = node.getNode<TextNode>(NAME).textValue()
        val value = node.findNode<TextNode>(VALUE)?.textValue().orEmpty()

        return Attribute(
            name = name,
            value = value,
            reference = node.get(REFERENCE)?.asBoolean().orFalse(),
            nameAttrs = node.get(NAME_ATTRIBUTES)?.let { mapper.convertValue(it, AttributeDetailsType) }
                ?: mutableListOf(),
            valueAttrs = node.get(VAL_ATTRIBUTES)?.let { mapper.convertValue(it, AttributeDetailsType) }
                ?: mutableListOf(),
        )
    }
}
