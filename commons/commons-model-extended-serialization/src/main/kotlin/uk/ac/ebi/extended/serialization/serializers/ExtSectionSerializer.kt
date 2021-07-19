package uk.ac.ebi.extended.serialization.serializers

import arrow.core.Either
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtSection
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACC_NO
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_LIST
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTIONS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType

// TODO serialize file list url
// TODO create client operations for reading the files
// TODO create ext section deserializer and deserialize the file list using the endpoint

class ExtSectionSerializer : JsonSerializer<ExtSection>() {
    override fun serialize(section: ExtSection, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField(ACC_NO, section.accNo)
        gen.writeStringField(TYPE, section.type)
        gen.writeObjectField(FILE_LIST, section.fileList)
        gen.writeObjectField(ATTRIBUTES, section.attributes)
        writeEitherList(SECTIONS, section.sections, gen)
        writeEitherList(FILES, section.files, gen)
        writeEitherList(LINKS, section.links, gen)
        gen.writeStringField(EXT_TYPE, ExtType.Section.type)
        gen.writeEndObject()
    }

    private fun writeEitherList(fieldName: String, list: List<Either<*, *>>, gen: JsonGenerator) {
        gen.writeArrayFieldStart(fieldName)
        list.forEach { gen.writeObject(it) }
        gen.writeEndArray()
    }
}
