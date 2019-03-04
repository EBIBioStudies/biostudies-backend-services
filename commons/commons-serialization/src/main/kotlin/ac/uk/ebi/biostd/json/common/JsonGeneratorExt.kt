package ac.uk.ebi.biostd.json.common

import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.base.ifTrue
import com.fasterxml.jackson.core.JsonGenerator as JsonGen

/**
 * Helper function to write a named json array from a given collection.
 */
inline fun <T> JsonGen.writeJsonArray(name: Any, values: Collection<T>, function: T.() -> Unit = ::writeObject) {
    if (values.isNotEmpty()) {
        writeArrayFieldStart(name.toString())
        values.forEach(function)
        writeEndArray()
    }
}

/**
 * Helper function to write a no named json array from a given collection.
 */
inline fun <T> JsonGen.writeJsonArray(values: Collection<T>, function: T.() -> Unit = ::writeObject) {
    if (values.isNotEmpty()) {
        writeStartArray()
        values.forEach(function)
        writeEndArray()
    }
}

/**
 * Helper function that wraps the creation of a json object using start and end object statements.
 */
inline fun JsonGen.writeObj(body: JsonGen.() -> Unit) {
    writeStartObject()
    body()
    writeEndObject()
}

/**
 * Helper functions used to write specific an string property type in json object, property is added only if string
 * is not null or empty.
 */
fun JsonGen.writeJsonString(name: Any, value: String?) = value.applyIfNotBlank { writeStringField(name.toString(), it) }

/**
 * Helper functions used to write specific an boolean property type in json object, property is added only if value
 * is true.
 */
fun JsonGen.writeJsonBoolean(name: Any, value: Boolean?) = value?.ifTrue { writeBooleanField(name.toString(), value) }

/**
 * Helper functions used to write specific numeric property type in json object, property is added only if value is
 * not null.
 */
fun JsonGen.writeJsonNumber(name: Any, value: Long?) = value?.let { writeNumberField(name.toString(), it) }

/**
 * Helper functions used to write specific object property type in json object, property is added only if value is not
 * null.
 */
fun JsonGen.writeJsonObject(name: Any, value: Any?) = value?.let { writeObjectField(name.toString(), it) }
