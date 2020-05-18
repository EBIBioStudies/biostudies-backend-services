package ebi.ac.uk.extended.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.File

class ExtFileSerializer : JsonSerializer<File>() {
    override fun serialize(value: File, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.absolutePath)
    }
}
