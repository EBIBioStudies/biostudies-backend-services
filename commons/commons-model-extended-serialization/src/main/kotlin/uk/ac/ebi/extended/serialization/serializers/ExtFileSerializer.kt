package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FILEPATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_ID
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FULL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_MD5
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_REL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_SIZE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType

class ExtFileSerializer : JsonSerializer<ExtFile>() {
    override fun serialize(file: ExtFile, gen: JsonGenerator, serializers: SerializerProvider) {
        when (file) {
            is NfsFile -> gen.serializeNfsFile(file)
            is FireFile -> gen.serializeFireFile(file)
        }
    }

    private fun JsonGenerator.serializeNfsFile(file: NfsFile) {
        writeStartObject()
        writeStringField(FILE_NAME, file.fileName)
        writeStringField(FILE_FILEPATH, file.filePath)
        writeStringField(FILE_REL_PATH, file.relPath)
        writeStringField(FILE_FULL_PATH, file.fullPath)
        writeObjectField(FILE_MD5, file.md5)
        writeObjectField(ATTRIBUTES, file.attributes)
        writeStringField(EXT_TYPE, ExtType.NfsFile.type)
        writeStringField(FILE_TYPE, file.type.value)
        writeNumberField(FILE_SIZE, file.size)
        writeEndObject()
    }

    private fun JsonGenerator.serializeFireFile(file: FireFile) {
        writeStartObject()
        writeStringField(FILE_NAME, file.fileName)
        writeStringField(FILE_FILEPATH, file.filePath)
        writeStringField(FILE_REL_PATH, file.relPath)
        writeStringField(FILE_FIRE_ID, file.fireId)
        writeObjectField(ATTRIBUTES, file.attributes)
        writeStringField(EXT_TYPE, ExtType.FireFile.type)
        writeStringField(FILE_TYPE, file.type.value)
        writeStringField(FILE_MD5, file.md5)
        writeNumberField(FILE_SIZE, file.size)
        writeEndObject()
    }
}
