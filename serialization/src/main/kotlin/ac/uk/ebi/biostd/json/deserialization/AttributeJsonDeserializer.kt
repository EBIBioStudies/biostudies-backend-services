package ac.uk.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

internal const val VAL_ATTRIBUTES = "valqual"
internal const val REFERENCE = "reference"
internal const val NAME = "name"
internal const val VALUE = "value"

class AttributeJsonDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        val name = node.get(NAME).asText()
        val value = node.get(VALUE).asText()
        val reference = node.get(REFERENCE)?.asBoolean() ?: false

        val listType = mapper.typeFactory.constructCollectionType(List::class.java, AttributeDetail::class.java)
        val valAttributes: List<AttributeDetail> = mapper.convertValue(node.get(VAL_ATTRIBUTES), listType) ?: listOf()

        return Attribute(name, value, reference, valueAttrs = valAttributes.toMutableList())
    }
}
