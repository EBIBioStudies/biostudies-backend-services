package ac.uk.ebi.biostd.ext

import ac.uk.ebi.biostd.common.StreamDeserializerBuilder
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken

fun JsonParser.forEachToken(function: () -> Unit) {
    while(nextToken() != JsonToken.END_OBJECT) {
        function()
    }
}

fun <T> JsonParser.mapFromBuilder(builder: StreamDeserializerBuilder<T>): List<T> {
    var token = nextToken()
    val elements: MutableList<T> = mutableListOf()

    if (token != JsonToken.START_ARRAY) {
        throw JsonParseException(this, "Arrays should start with ${JsonToken.START_ARRAY}")
    }

    while(token != JsonToken.END_ARRAY) {
        elements.add(readFromBuilder(builder))
    }

    return elements.toList()
}

// TODO check this, is getting null forever in JSON deserialization
fun <T> JsonParser.readFromBuilder(builder: StreamDeserializerBuilder<T>): T {
    forEachToken {
        val fieldName = getCurrentFieldName()
        fieldName?.let {
            builder.loadField(it, this)
        }
    }
    return builder.build()
}

fun JsonParser.getCurrentFieldName(): String? {
    val field = currentName
    nextToken()

    return field
}

fun JsonParser.getTrimmedText() = text.trim()
