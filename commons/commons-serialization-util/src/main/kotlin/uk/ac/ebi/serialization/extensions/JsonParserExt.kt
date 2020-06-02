package uk.ac.ebi.serialization.extensions

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

/**
 * Iterates over the array and parses each element applying the given parsing function.
 *
 * @param function The function that will be used to parse each array element.
 */
fun <T> JsonParser.parseArray(function: (JsonParser) -> T): List<T> {
    val elements = mutableListOf<T>()
    require(nextToken() == JsonToken.START_ARRAY) { "Expected start array character" }

    while (nextToken() != JsonToken.END_ARRAY) {
        elements.add(function(this))
    }

    return elements
}

/**
 * Iterates over an array applying the given function to each element.
 *
 * @param function The function to be applied to each element.
 */
fun JsonParser.forEach(function: (JsonParser) -> Unit) {
    require(nextToken() == JsonToken.START_ARRAY) { "Expected start array character" }

    while (nextToken() != JsonToken.END_ARRAY) {
        function(this)
    }
}
