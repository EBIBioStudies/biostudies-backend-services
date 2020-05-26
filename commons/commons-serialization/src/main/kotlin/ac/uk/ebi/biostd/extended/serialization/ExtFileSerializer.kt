package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.ATTRIBUTES
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.EXT_TYPE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILE
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILE_NAME
import ac.uk.ebi.biostd.extended.constants.ExtType
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFile

class ExtFileSerializer : JsonSerializer<ExtFile>() {
    override fun serialize(file: ExtFile, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField(FILE_NAME, file.fileName)
        gen.writeObjectField(FILE, file.file.absolutePath)
        gen.writeObjectField(ATTRIBUTES, file.attributes)
        gen.writeStringField(EXT_TYPE, ExtType.File.type)
        gen.writeEndObject()
    }
}
