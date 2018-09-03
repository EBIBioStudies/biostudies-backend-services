package ac.uk.ebi.biostd.extensions

import arrow.core.None
import arrow.core.Option
import arrow.core.Try
import arrow.core.getOrElse
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

fun ObjectMapper.tryConvertValue(node: JsonNode, type: JavaType): Option<Any> {
    return Try<Any?> { this.convertValue(node, type) }.map { Option.fromNullable(it) }.getOrElse { None }
}
