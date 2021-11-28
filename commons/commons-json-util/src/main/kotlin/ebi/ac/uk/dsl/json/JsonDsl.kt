package ebi.ac.uk.dsl.json

interface JsonVal

class JsonString(private val value: String) : JsonVal {
    override fun toString() = value.toJsonQuote()
}

class JsonBool(private val boolean: Boolean) : JsonVal {
    override fun toString() = if (boolean) "true" else "false"
}

class JsonNumber(private val number: Number) : JsonVal {
    override fun toString() = number.toString()
}

object JsonNull : JsonVal {
    override fun toString() = "null"
}

class JsonArray(private val elements: List<JsonVal>) : JsonVal {
    override fun toString() = elements.joinToString(prefix = "[", postfix = "]") { it.toString() }
}

class JsonObject(private val elements: MutableMap<String, JsonVal> = mutableMapOf()) : JsonVal {

    override fun toString() =
        elements.entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> """"$key": $value""" }

    @JvmName("addAttribute")
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

    infix fun String.to(value: JsonNull) {
        elements[this] = value
    }

    infix fun String.to(value: JsonObject) {
        elements[this] = value
    }

    infix fun String.to(value: JsonObject.() -> Unit) {
        elements[this] = JsonObject().apply(value)
    }

    infix fun String.to(value: Enum<*>) {
        elements[this] = JsonString(value.name)
    }
}

val jsonNull get() = JsonNull

fun jsonObj(bodyFun: JsonObject.() -> Unit) = JsonObject().apply(bodyFun)
