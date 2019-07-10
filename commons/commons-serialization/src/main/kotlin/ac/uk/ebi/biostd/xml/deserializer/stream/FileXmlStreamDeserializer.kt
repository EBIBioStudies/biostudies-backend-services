package ac.uk.ebi.biostd.xml.deserializer.stream

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.AttributeFields
import ebi.ac.uk.model.constants.FileFields

internal class FileXmlStreamDeserializer : StdDeserializer<File>(File::class.java) {
    // TODO mandatory field validation.
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext): File {
        var path = ""
        val attributes: MutableList<Attribute> = mutableListOf()

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            val field = parser.currentName

            parser.nextToken()

            when (field) {
                FileFields.PATH.value -> path = parser.text.trim()
                AttributeFields.ATTRIBUTE.value -> attributes.add(parser.readValueAs(Attribute::class.java))
            }
        }

        return File(path, attributes = attributes.toList())
    }
}
