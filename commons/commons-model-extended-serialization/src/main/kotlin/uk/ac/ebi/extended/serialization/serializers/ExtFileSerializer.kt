package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.FileUtils
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_DIR_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FILE_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_SIZE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType
import java.io.File

class ExtFileSerializer : JsonSerializer<ExtFile>() {
    override fun serialize(file: ExtFile, gen: JsonGenerator, serializers: SerializerProvider) {
        when (file) {
            is NfsFile -> serializeNfsFile(file, gen)
            is FireFile -> TODO()
        }
    }

    private fun serializeNfsFile(file: NfsFile, gen: JsonGenerator) {
        gen.writeStartObject()
        gen.writeStringField(FILE_NAME, file.file.name)
        gen.writeStringField(FILE_PATH, file.fileName)
        gen.writeObjectField(FILE, file.file.absolutePath)
        gen.writeObjectField(ATTRIBUTES, file.attributes)
        gen.writeStringField(EXT_TYPE, ExtType.NfsFile.type)
        gen.writeStringField(FILE_TYPE, getType(file.file))
        gen.writeNumberField(FILE_SIZE, FileUtils.size(file.file))
        gen.writeEndObject()
    }

    private fun getType(file: File) = if (FileUtils.isDirectory(file)) FILE_DIR_TYPE else FILE_FILE_TYPE
}
