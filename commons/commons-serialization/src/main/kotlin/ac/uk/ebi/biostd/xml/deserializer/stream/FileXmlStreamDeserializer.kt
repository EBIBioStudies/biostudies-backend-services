package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.validation.InvalidFileListElementException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.AttributeFields
import ebi.ac.uk.model.constants.FileFields

internal class FileXmlStreamDeserializer : StdDeserializer<File>(File::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): File {
        var path = ""
        val attributes: MutableList<Attribute> = mutableListOf()

        while (p!!.nextToken() != JsonToken.END_OBJECT) {
            val field = p.currentName

            p.nextToken()

            when (field) {
                FileFields.PATH.value -> path = p.text.trim()
                AttributeFields.ATTRIBUTE.value -> attributes.add(p.readValueAs(Attribute::class.java))
            }
        }

        // TODO this should have a serialization context rather than fail the whole process.
        //  Same for other formats
        path.ifBlank { throw InvalidFileListElementException("File path is mandatory") }

        return File(path, attributes = attributes.toList())
    }
}
