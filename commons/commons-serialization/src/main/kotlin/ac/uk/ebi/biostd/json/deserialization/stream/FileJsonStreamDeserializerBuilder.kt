package ac.uk.ebi.biostd.json.deserialization.stream

import ac.uk.ebi.biostd.common.deserialization.stream.AttributeStreamDeserializerBuilder
import ac.uk.ebi.biostd.common.deserialization.stream.StreamDeserializerBuilder
import ac.uk.ebi.biostd.ext.getTrimmedText
import ac.uk.ebi.biostd.ext.mapFromBuilder
import com.fasterxml.jackson.core.JsonParser
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

internal class FileJsonStreamDeserializerBuilder(
    private val attributeStreamDeserializerBuilder: AttributeStreamDeserializerBuilder
) : StreamDeserializerBuilder<File>() {
    override fun loadField(fieldName: String, parser: JsonParser) {
        when (fieldName) {
            FileFields.ATTRIBUTES.value ->
                attributes.addAll(parser.mapFromBuilder(attributeStreamDeserializerBuilder))
            else -> fields[fieldName] = parser.getTrimmedText()
        }
    }

    override fun build(): File {
        val fileAttrs: MutableList<Attribute> = mutableListOf()
        attributes.forEach { fileAttrs.add(it) }
        attributes.clear()

        return File(fields[FileFields.PATH.value]!!, attributes = fileAttrs)
    }
}
