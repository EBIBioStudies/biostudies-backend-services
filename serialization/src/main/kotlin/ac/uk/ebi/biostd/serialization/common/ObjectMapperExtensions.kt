package ac.uk.ebi.biostd.serialization.common

import arrow.core.None
import arrow.core.Option
import arrow.core.Try
import arrow.core.getOrElse
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Try to convert the given node using the provided type, return an optional with conversion or emtpy if type could not
 * be converter.
 *
 */
fun ObjectMapper.tryConvertValue(node: JsonNode, type: JavaType): Option<Any> {
    return Try<Any> { convertValue(node, type) }.map { Option.fromNullable(it) }.getOrElse { None }
}
