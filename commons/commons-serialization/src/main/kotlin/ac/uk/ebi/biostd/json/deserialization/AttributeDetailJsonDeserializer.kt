package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.deserialization.AttributeJsonDeserializerHelper.deserializeNameValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.AttributeDetail

internal class AttributeDetailJsonDeserializer : StdDeserializer<AttributeDetail>(AttributeDetail::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): AttributeDetail {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val (name, value) = deserializeNameValue(node)

        return AttributeDetail(name, value)
    }
}
