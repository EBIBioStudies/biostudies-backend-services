package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.ACC_NO
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.ATTRIBUTES
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.EXT_TYPE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILES
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILE_LIST
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.LINKS
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.SECTIONS
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.TYPE
import ac.uk.ebi.biostd.extended.constants.ExtType
import arrow.core.Either
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtSection

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
