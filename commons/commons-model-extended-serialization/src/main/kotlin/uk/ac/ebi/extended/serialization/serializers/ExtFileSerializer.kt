package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFile
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtType

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
