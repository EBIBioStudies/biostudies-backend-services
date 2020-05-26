package ac.uk.ebi.biostd.extended.serialization

import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILES
import ac.uk.ebi.biostd.extended.constants.ExtSerializationFields.FILE_NAME
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.extended.model.ExtFileList

class ExtFileListSerializer : JsonSerializer<ExtFileList>() {
    override fun serialize(fileList: ExtFileList, gen: JsonGenerator, serializers: SerializerProvider) {
        val basePath = fileList.files.first().file.absolutePath.substringBefore("/Files")
        gen.writeStartObject()
        gen.writeStringField(FILE_NAME, "$basePath/${fileList.fileName}")
        gen.writeObjectField(FILES, fileList.files)
        gen.writeEndObject()
    }
}
