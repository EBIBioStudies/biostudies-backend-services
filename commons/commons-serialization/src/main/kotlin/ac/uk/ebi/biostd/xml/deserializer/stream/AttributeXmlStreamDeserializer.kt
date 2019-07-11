package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.common.deserialization.stream.AttributeStreamDeserializerBuilder
import ac.uk.ebi.biostd.ext.readFromBuilder
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute

internal class AttributeXmlStreamDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?) =
        parser.readFromBuilder(AttributeStreamDeserializerBuilder())
}
