package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.serialization.json.common.array
import ac.uk.ebi.biostd.serialization.json.common.writeField
import ac.uk.ebi.biostd.serialization.json.common.writeObj
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.Term
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.base.whenTrue

internal const val TERMS = "valqual"
internal const val REFERENCE = "isReference"
internal const val NAME = "name"
internal const val VALUE = "value"

class AttributeJsonSerializer : StdSerializer<Attribute>(Attribute::class.java) {

    override fun isEmpty(provider: SerializerProvider, value: Attribute): Boolean = value.name.isEmpty()

    override fun serialize(attr: Attribute, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObj {
            writeField(NAME, attr.name)
            writeField(VALUE, attr.value)
            attr.reference.whenTrue { gen.writeBooleanField(REFERENCE, attr.reference) }
            array(TERMS, attr.terms, gen::writeObject)
        }
    }
}

class AttributeJsonDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        val name = node.get(NAME).asText()
        val value = node.get(VALUE).asText()
        val reference = node.get(REFERENCE)?.asBoolean() ?: false

        val listType = mapper.typeFactory.constructCollectionType(List::class.java, Term::class.java)
        val terms: List<Term> = mapper.convertValue(node.get(TERMS), listType) ?: listOf()

        return Attribute(name, value, reference, terms)
    }
}
