package ac.uk.ebi.biostd.ext

import ac.uk.ebi.biostd.common.deserialization.stream.StreamDeserializerBuilder
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

/**
 * Iterates over each element loaded by the parser and applies the given function until the {@link JsonToken.END_OBJECT}
 * character is found.
 *
 * @param function The function to be applied to each element.
 */
fun JsonParser.forEachToken(function: () -> Unit) {
    while (nextToken() != JsonToken.END_OBJECT) {
        function()
    }
}

/**
 * Validates whether or not the element loaded in the parser is a valid array and moves the parser to the next token.
 */
fun JsonParser.startArray() {
    if (nextToken() != JsonToken.START_ARRAY) {
        throw JsonParseException(this, "Arrays should start with ${JsonToken.START_ARRAY}")
    }
}

/**
 * Iterates over an array element and maps it to a list containing elements built with the given builder.
 *
 * @param builder The builder that will be used to map the elements in the array.
 */
fun <T> JsonParser.mapFromBuilder(builder: StreamDeserializerBuilder<T>): List<T> {
    val elements: MutableList<T> = mutableListOf()

    var token = nextToken()
    while (token != JsonToken.END_ARRAY) {
        elements.add(readFromBuilder(builder))
        token = nextToken()
    }

    return elements.toList()
}

/**
 * Creates an element with the given builder using the current parser object.
 *
 * @param builder The builder that will be used to create the element.
 */
fun <T> JsonParser.readFromBuilder(builder: StreamDeserializerBuilder<T>): T {
    forEachToken {
        val fieldName = getCurrentFieldName()
        fieldName?.let {
            builder.loadField(it, this)
        }
    }

    return builder.build()
}

/**
 * Returns the name of the current field in the parser and moves to the next token.
 */
fun JsonParser.getCurrentFieldName(): String? {
    val field = currentName
    nextToken()

    return field
}

/**
 * Returns the trimmed value of the current field.
 */
fun JsonParser.getTrimmedText() = text.trim()
