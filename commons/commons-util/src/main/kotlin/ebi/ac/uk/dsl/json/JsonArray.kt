package ebi.ac.uk.dsl.json

fun jsonArray(vararg elements: JsonVal) = JsonArray(elements.toList())
fun jsonArray(vararg elements: Number) = JsonArray(elements.map { JsonNumber(it) })
fun jsonArray(vararg elements: String) = JsonArray(elements.map { JsonString(it) })
fun jsonArray(vararg elements: Any) = JsonArray(elements.map { asJsonValue(it) })
fun jsonArray(vararg elements: JsonObject.() -> Unit) = JsonArray(elements.map { JsonObject().apply(it) })

private fun asJsonValue(value: Any) = when (value) {
    is String -> JsonString(value)
    is Number -> JsonNumber(value)
    is JsonVal -> value
    else -> JsonString(value.toString())
}
