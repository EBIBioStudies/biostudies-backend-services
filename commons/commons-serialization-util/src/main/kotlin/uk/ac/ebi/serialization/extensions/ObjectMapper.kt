package uk.ac.ebi.serialization.extensions

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

fun <T : Any> ObjectMapper.serializeList(
    files: Sequence<T>,
    outputStream: OutputStream,
): Int {
    val jsonGenerator = factory.createGenerator(outputStream)
    jsonGenerator.use {
        it.writeStartArray()
        val count = files.onEach { file -> writeValue(it, file) }.count()
        it.writeEndArray()
        return count
    }
}

suspend fun <T : Any> ObjectMapper.serializeFlow(
    files: Flow<T>,
    outputStream: OutputStream,
): Int =
    withContext(Dispatchers.IO) {
        val jsonGenerator = factory.createGenerator(outputStream)
        val count = AtomicInteger(0)
        jsonGenerator.use {
            it.writeStartArray()
            files
                .collect { file ->
                    count.getAndIncrement()
                    writeValue(it, file)
                }
            it.writeEndArray()
        }
        count.get()
    }

inline fun <reified T> ObjectMapper.convertOrDefault(
    node: JsonNode,
    property: String,
    default: () -> T,
): T =
    when (val propertyNode: JsonNode? = node.findNode(property)) {
        null -> default()
        else -> convertValue(propertyNode)
    }

inline fun <reified T : Any> ObjectMapper.deserializeAsFlow(inputStream: InputStream): Flow<T> {
    val jsonParser = factory.createParser(inputStream)
    if (jsonParser.nextToken() != JsonToken.START_ARRAY) error("Expected content to be an array")
    return flow<T> {
        var next = jsonParser.nextToken()
        while (next != null && next != JsonToken.END_ARRAY) {
            emit(readValue(jsonParser, T::class.java))
            next = jsonParser.nextToken()
        }
    }.flowOn(Dispatchers.IO)
}

inline fun <reified T : Any> ObjectMapper.deserializeAsSequence(inputStream: InputStream): Sequence<T> {
    val jsonParser = factory.createParser(inputStream)
    if (jsonParser.nextToken() != JsonToken.START_ARRAY) error("Expected content to be an array")
    return asSequence(jsonParser)
}

inline fun <reified T : Any> ObjectMapper.asSequence(jsonParser: JsonParser): Sequence<T> =
    object : Sequence<T> {
        override fun iterator(): Iterator<T> =
            object : Iterator<T> {
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
fun ObjectMapper.tryConvertValue(
    node: JsonNode,
    type: JavaType,
): Any? {
    return runCatching<Any> { convertValue(node, type) }.getOrNull()
}
