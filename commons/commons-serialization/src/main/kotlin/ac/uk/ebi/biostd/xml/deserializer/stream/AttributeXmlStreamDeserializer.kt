package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.VALUE
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute

internal class AttributeXmlStreamDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): Attribute {
        var name = ""
        var value = ""

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val field = parser.currentName

            parser.nextToken()

            when (field) {
                NAME -> name = parser.text.trim()
                VALUE -> value = parser.text.trim()
            }
        }

        return Attribute(name, value)
    }
}
