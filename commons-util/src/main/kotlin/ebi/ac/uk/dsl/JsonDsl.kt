package ebi.ac.uk.dsl

sealed class JsonVal

data class JsonString(val value: String) : JsonVal() {
    override fun toString() = value.toJsonQuote()
}

data class JsonBool(val boolean: Boolean) : JsonVal() {
    override fun toString() = if (boolean) "true" else "false"
}

data class JsonNumber(val number: Number) : JsonVal() {
    override fun toString() = number.toString()
}

object JsonNull : JsonVal() {
    override fun toString() = "null"
}

data class JsonArray(val elements: List<JsonVal>) : JsonVal() {
    override fun toString() = elements.joinToString(prefix = "[", postfix = "]") { it.toString() }
}

data class JsonObject(val elements: MutableMap<String, JsonVal> = mutableMapOf()) : JsonVal() {

    override fun toString() = elements.toJsonString()

    infix fun String.to(value: String) {
        elements[this] = JsonString(value)
    }

    infix fun String.to(value: Number) {
        elements[this] = JsonNumber(value)
    }

    infix fun String.to(value: Boolean) {
        elements[this] = JsonBool(value)
    }

    infix fun String.to(value: JsonArray) {
        elements[this] = value
    }

    infix fun String.to(value: JsonObject.() -> Unit) {
        elements[this] = JsonObject().apply(value)
    }
}
