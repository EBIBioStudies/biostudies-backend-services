package ac.uk.ebi.biostd.serialization.json.common

import com.fasterxml.jackson.core.JsonGenerator

fun JsonGenerator.writeField(name: String, value: String?) = value?.let { writeStringField(name, it) }

inline fun <T> JsonGenerator.array(name: String, values: List<T>, function: T.() -> Unit) {
    if (values.isNotEmpty()) {
        writeArrayFieldStart(name)
        values.forEach(function)
        writeEndArray()
    }
}

inline fun <T> JsonGenerator.array(values: List<T>, function: T.() -> Unit) {
    if (values.isNotEmpty()) {
        writeStartArray()
        values.forEach(function)
        writeEndArray()
    }
}

inline fun JsonGenerator.writeObj(function: JsonGenerator.() -> Unit) {
    writeStartObject()
    function()
    writeEndObject()
}
