package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.json.exception.NoAttributeValueException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.model.AttributeDetail
import uk.ac.ebi.serialization.extensions.getNode

internal class AttributeDetailJsonDeserializer : StdDeserializer<AttributeDetail>(AttributeDetail::class.java) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): AttributeDetail {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val name = node.getNode<TextNode>(NAME).textValue()
        val value = node.getNode<TextNode>(VALUE).textValue()
        require(value.isNotBlank()) { throw NoAttributeValueException(name) }
        return AttributeDetail(name, value)
    }
}
