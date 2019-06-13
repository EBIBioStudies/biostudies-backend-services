package ac.uk.ebi.biostd.ext

import arrow.core.None
import arrow.core.Option
import arrow.core.Try
import arrow.core.getOrElse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue

/**
 * Try to convert the given node using the provided type, return an optional with conversion or emtpy if type could not
 * be converter.
 *
 */
internal fun ObjectMapper.tryConvertValue(node: JsonNode, type: JavaType): Option<Any> {
    return Try<Any> { convertValue(node, type) }.map { Option.fromNullable(it) }.getOrElse { None }
}

/**
 * Obtain the collection type for the given value.
 */
internal fun ObjectMapper.getListType(type: Class<*>) = typeFactory.constructCollectionType(List::class.java, type)!!

/**
 * Null safe list converter. Convert the node to the list type of values. If node is not found empty mutable list
 * is retrieved.
 */
internal inline fun <reified T> ObjectMapper.convertList(node: JsonNode?) =
    if (node != null) convertValue(node, getListType(T::class.java)) else mutableListOf<T>()

internal inline fun <reified T> ObjectMapper.convertNode(node: JsonNode?): T? = node?.let { convertValue(node) }

internal inline fun <reified T> ObjectMapper.convertList(node: JsonNode?, sectionsType: TypeReference<*>) =
    if (node != null) convertValue(node, sectionsType) else mutableListOf<T>()

internal inline fun <reified T : Any> ObjectMapper.deserialize(json: String) = readValue(json, T::class.java)!!
internal inline fun <reified T> ObjectMapper.serialize(value: T): String = writeValueAsString(value)
