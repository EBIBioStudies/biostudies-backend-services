package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFileList
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME

class ExtFileListSerializer : JsonSerializer<ExtFileList>() {
    override fun serialize(fileList: ExtFileList, gen: JsonGenerator, serializers: SerializerProvider) {
        val basePath = fileList.files.first().file.absolutePath.substringBefore("/Files")
        gen.writeStartObject()
        gen.writeStringField(FILE_NAME, "$basePath/${fileList.fileName}")
        gen.writeObjectField(FILES, fileList.files)
        gen.writeEndObject()
    }
}
