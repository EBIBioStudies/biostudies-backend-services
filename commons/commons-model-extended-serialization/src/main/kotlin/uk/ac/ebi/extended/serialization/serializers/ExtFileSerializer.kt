package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_DIR_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FILE_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_ID
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_MD5
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_SIZE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType
import java.io.File

class ExtFileSerializer : JsonSerializer<ExtFile>() {
    override fun serialize(file: ExtFile, gen: JsonGenerator, serializers: SerializerProvider) {
        when (file) {
            is NfsFile -> gen.serializeNfsFile(file)
            is FireFile -> gen.serializeFireFile(file)
            is FireDirectory -> gen.serializeFireDirectory(file)
        }
    }

    private fun JsonGenerator.serializeNfsFile(file: NfsFile) {
        writeStartObject()
        writeStringField(FILE_NAME, file.file.name)
        writeStringField(FILE_PATH, file.fileName)
        writeObjectField(FILE, file.file.absolutePath)
        writeObjectField(ATTRIBUTES, file.attributes)
        writeStringField(EXT_TYPE, ExtType.NfsFile.type)
        writeStringField(FILE_TYPE, getType(file.file))
        writeNumberField(FILE_SIZE, FileUtils.size(file.file))
        writeEndObject()
    }

    private fun JsonGenerator.serializeFireFile(file: FireFile) {
        writeStartObject()
        writeStringField(FILE_NAME, file.fileName)
        writeStringField(FILE_FIRE_ID, file.fireId)
        writeObjectField(ATTRIBUTES, file.attributes)
        writeStringField(EXT_TYPE, ExtType.FireFile.type)
        writeStringField(FILE_TYPE, FILE_FILE_TYPE)
        writeStringField(FILE_MD5, file.md5)
        writeNumberField(FILE_SIZE, file.size)
        writeEndObject()
    }

    private fun JsonGenerator.serializeFireDirectory(file: FireDirectory) {
        writeStartObject()
        writeStringField(FILE_NAME, file.fileName)
        writeObjectField(ATTRIBUTES, file.attributes)
        writeStringField(EXT_TYPE, ExtType.FireDirectory.type)
        writeStringField(FILE_TYPE, FILE_DIR_TYPE)
        writeStringField(FILE_MD5, file.md5)
        writeNumberField(FILE_SIZE, file.size)
        writeEndObject()
    }

    private fun getType(file: File) = if (FileUtils.isDirectory(file)) FILE_DIR_TYPE else FILE_FILE_TYPE
}
