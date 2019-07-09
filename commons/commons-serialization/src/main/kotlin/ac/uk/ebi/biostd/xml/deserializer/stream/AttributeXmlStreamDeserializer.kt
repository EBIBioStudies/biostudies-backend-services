package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.validation.InvalidFileListElementException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute

internal class AttributeXmlStreamDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Attribute {
        var name = ""
        var value = ""

        while (p!!.nextToken() != JsonToken.END_OBJECT) {
            val field = p.currentName

            p.nextToken()

            when (field) {
                NAME -> name = p.text.trim()
                VALUE -> value = p.text.trim()
            }
        }

        value.ifBlank { throw InvalidFileListElementException("Attribute value is mandatory") }

        return Attribute(name, value)
    }
}
