package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.NAME_ATTRIBUTES
import ac.uk.ebi.biostd.common.REFERENCE
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.common.VAL_ATTRIBUTES
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.getNode

internal class AttributeJsonDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return Attribute(
            name = node.getNode<TextNode>(NAME).textValue(),
            value = node.getNode<TextNode>(VALUE).textValue(),
            reference = node.get(REFERENCE)?.asBoolean().orFalse(),
            valueAttrs = mapper.convertList(node.get(VAL_ATTRIBUTES)),
            nameAttrs = mapper.convertList(node.get(NAME_ATTRIBUTES)))
    }
}
