package ac.uk.ebi.biostd.extensions

import com.fasterxml.jackson.core.JsonGenerator
import ebi.ac.uk.base.whenTrue

inline fun JsonGenerator.writeObj(body: JsonGenerator.() -> Unit) {
    writeStartObject()
    body()
    writeEndObject()
}

inline fun <T> JsonGenerator.writeArrayFieldIfNotEmpty(name: String, values: List<T>, function: T.() -> Unit) {
    if (values.isNotEmpty()) {
        writeArrayFieldStart(name)
        values.forEach(function)
        writeEndArray()
    }
}

fun JsonGenerator.writeStringFieldIfNotEmpty(name: String, value: String?) = value.takeIf { !it.isNullOrEmpty() }?.let { writeStringField(name, it) }

fun JsonGenerator.writeBooleanFieldIfNotEmpty(name: String, value: Boolean?) = value?.whenTrue { writeBooleanField(name, value) }

fun JsonGenerator.writeNumberFieldIfNotEmpty(name: String, value: Long?) = value?.let { writeNumberField(name, it) }
