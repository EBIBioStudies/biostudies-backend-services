package uk.ac.ebi.serialization.extensions

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

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
