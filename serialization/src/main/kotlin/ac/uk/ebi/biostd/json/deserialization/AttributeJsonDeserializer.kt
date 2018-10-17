package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.SimpleAttribute
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

internal const val TERMS = "valqual"
internal const val REFERENCE = "isReference"
internal const val NAME = "name"
internal const val VALUE = "value"

class AttributeJsonDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        val name = node.get(NAME).asText()
        val value = node.get(VALUE).asText()
        val reference = node.get(REFERENCE)?.asBoolean() ?: false

        val listType = mapper.typeFactory.constructCollectionType(List::class.java, SimpleAttribute::class.java)
        val terms: List<SimpleAttribute> = mapper.convertValue(node.get(TERMS), listType) ?: listOf()

        return Attribute(name, value, reference, terms)
    }
}
