package ac.uk.ebi.biostd.json.deserialization.stream

import ac.uk.ebi.biostd.json.JsonSerializer
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import ebi.ac.uk.model.File
import java.lang.reflect.Type

internal class FileJsonStreamDeserializer(private val jsonSerializer: JsonSerializer) : JsonDeserializer<File> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) =
        jsonSerializer.deserialize<File>(json.toString())
}
