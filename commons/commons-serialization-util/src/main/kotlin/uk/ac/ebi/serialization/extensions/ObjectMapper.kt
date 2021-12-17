package uk.ac.ebi.serialization.extensions

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import java.io.InputStream
import java.io.OutputStream

fun <T : Any> ObjectMapper.serializeList(files: Sequence<T>, outputStream: OutputStream) {
    val jsonGenerator = factory.createGenerator(outputStream)

    jsonGenerator.use {
        it.writeStartArray()
        files.forEach { file -> writeValue(it, file) }
        it.writeEndArray()
    }
}

inline fun <reified T : Any> ObjectMapper.deserializeList(inputStream: InputStream): Sequence<T> {
    val jsonParser = factory.createParser(inputStream)

    if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
        throw IllegalStateException("Expected content to be an array")
    }

    return sequence {
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            yield(readValue(jsonParser, T::class.java))
        }
    }
}

/**
 * Try to convert the given node using the provided type, return an optional with conversion or emtpy if type could not
 * be converter.
 *
 */
fun ObjectMapper.tryConvertValue(node: JsonNode, type: JavaType): Any? {
    return runCatching<Any> { convertValue(node, type) }.getOrNull()
}

/**
 * Obtain the collection type for the given value.
 */
fun ObjectMapper.getListType(type: Class<*>) = typeFactory.constructCollectionType(List::class.java, type)!!

/**
 * Null safe list converter. Convert the node to the list type of values. If node is not found empty mutable list
 * is retrieved.
 */
inline fun <reified T> ObjectMapper.convertList(node: JsonNode?) =
    if (node != null) convertValue(node, getListType(T::class.java)) else mutableListOf<T>()

inline fun <reified T> ObjectMapper.convertNode(node: JsonNode?): T? = node?.let { convertValue(node) }

inline fun <reified T> ObjectMapper.convertList(node: JsonNode?, sectionsType: TypeReference<*>) =
    if (node != null) convertValue(node, sectionsType) else mutableListOf<T>()

inline fun <reified T : Any> ObjectMapper.deserialize(json: String) = readValue(json, T::class.java)!!
inline fun <reified T> ObjectMapper.serialize(value: T): String = writeValueAsString(value)
