package ac.uk.ebi.biostd.serialization.json.common

import com.fasterxml.jackson.core.JsonGenerator
import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.base.whenTrue

inline fun JsonGenerator.writeObj(body: JsonGenerator.() -> Unit) {
    writeStartObject()
    body()
    writeEndObject()
}

inline fun <T> JsonGenerator.writeJsonArray(name: Any, values: Collection<T>, function: T.() -> Unit = this::writeObject) {
    if (values.isNotEmpty()) {
        writeArrayFieldStart(name.toString())
        values.forEach(function)
        writeEndArray()
    }
}

inline fun <T> JsonGenerator.writeJsonArray(values: Collection<T>, function: T.() -> Unit = this::writeObject) {
    if (values.isNotEmpty()) {
        writeStartArray()
        values.forEach(function)
        writeEndArray()
    }
}

fun JsonGenerator.writeJsonString(name: Any, value: String?) = value.applyIfNotBlank { writeStringField(name.toString(), it) }
fun JsonGenerator.writeJsonBoolean(name: Any, value: Boolean?) = value?.whenTrue { writeBooleanField(name.toString(), value) }
fun JsonGenerator.writeJsonNumber(name: Any, value: Long?) = value?.let { writeNumberField(name.toString(), it) }
fun JsonGenerator.writeJsonObject(name: Any, value: Any?) = value?.let { writeObjectField(name.toString(), it) }

