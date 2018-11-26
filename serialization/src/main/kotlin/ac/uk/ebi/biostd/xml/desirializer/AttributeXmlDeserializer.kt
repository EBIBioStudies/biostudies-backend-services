package ac.uk.ebi.biostd.xml.desirializer

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.NAME_ATTRIBUTES
import ac.uk.ebi.biostd.common.REFERENCE
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.common.VAL_ATTRIBUTES
import ac.uk.ebi.biostd.ext.convertList
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

private const val NODE_NAME = "attribute"

class AttributeXmlDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Attribute {
        val mapper = jp.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jp)[NODE_NAME]

        return Attribute(
            name = node.get(NAME).asText(),
            value = node.get(VALUE).asText(),
            reference = node.get(REFERENCE)?.asBoolean().orFalse(),
            valueAttrs = mapper.convertList(node.get(VAL_ATTRIBUTES), AttributeDetail::class.java),
            nameAttrs = mapper.convertList(node.get(NAME_ATTRIBUTES), AttributeDetail::class.java))
    }
}