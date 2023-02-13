package uk.ac.ebi.serialization.extensions

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import java.io.InputStream
import java.io.OutputStream

fun <T : Any> ObjectMapper.serializeList(files: Sequence<T>, outputStream: OutputStream): Int {
    val jsonGenerator = factory.createGenerator(outputStream)
    var filesCount = 0
    jsonGenerator.use {
        it.writeStartArray()
        filesCount = files.onEach { file -> writeValue(it, file) }.count()
        it.writeEndArray()
    }

    return filesCount
}

inline fun <reified T> ObjectMapper.convertOrDefault(node: JsonNode, property: String, default: () -> T): T =
    when (val propertyNode: JsonNode? = node.findNode(property)) {
        null -> default()
        else -> convertValue(propertyNode)
    }

inline fun <reified T : Any> ObjectMapper.deserializeList(inputStream: InputStream): Sequence<T> {
    val jsonParser = factory.createParser(inputStream)
    if (jsonParser.nextToken() != JsonToken.START_ARRAY) throw IllegalStateException("Expected content to be an array")
    return asSequence(jsonParser)
}

inline fun <reified T : Any> ObjectMapper.asSequence(jsonParser: JsonParser): Sequence<T> =
    object : Sequence<T> {
        override fun iterator(): Iterator<T> = object : Iterator<T> {
            private var next: JsonToken? = jsonParser.nextToken()

            override fun hasNext(): Boolean {
                return next != null && next != JsonToken.END_ARRAY
            }

            override fun next(): T {
                if (hasNext().not()) throw NoSuchElementException()
                val value = readValue(jsonParser, T::class.java)
                next = jsonParser.nextToken()
                return value
            }
        }
    }

/**
 * Try to convert the given node using the provided type, return an optional with conversion or emtpy if type could not
 * be converter.
 */
fun ObjectMapper.tryConvertValue(node: JsonNode, type: JavaType): Any? {
    return runCatching<Any> { convertValue(node, type) }.getOrNull()
}
