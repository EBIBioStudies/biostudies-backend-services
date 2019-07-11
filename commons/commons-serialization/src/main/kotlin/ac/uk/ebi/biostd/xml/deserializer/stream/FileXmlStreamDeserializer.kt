package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.common.deserialization.stream.StreamDeserializerBuilder
import ac.uk.ebi.biostd.ext.readFromBuilder
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

internal class FileXmlStreamDeserializer : StdDeserializer<File>(File::class.java) {
    // TODO mandatory field validation.
    override fun deserialize(parser: JsonParser, ctxt: DeserializationContext?) =
        parser.readFromBuilder(FileStreamDeserializerBuilder())
}

internal class FileStreamDeserializerBuilder : StreamDeserializerBuilder<File>() {
    override fun build(): File {
        val fileAttrs: MutableList<Attribute> = mutableListOf()
        attributes.forEach { fileAttrs.add(it) }
        attributes.clear()

        return File(fields[FileFields.PATH.value]!!, attributes = fileAttrs)
    }
}
