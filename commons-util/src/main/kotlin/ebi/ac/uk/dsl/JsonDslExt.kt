package ebi.ac.uk.dsl

fun jsonObj(body: JsonObject.() -> Unit) = JsonObject().apply { body() }

fun jsonArray(vararg elements: JsonObject.() -> Unit) = JsonArray(elements.map { JsonObject().apply(it) }.toMutableList())
fun jsonArray(vararg elements: JsonVal) = JsonArray(elements.toMutableList())
fun jsonArray(vararg elements: Number) = JsonArray(elements.mapTo(mutableListOf()) { JsonNumber(it) })
fun jsonArray(vararg elements: String) = JsonArray(elements.mapTo(mutableListOf()) { JsonString(it) })

fun Map<String, JsonVal>.toJsonString() =
    entries.joinToString(prefix = "{", postfix = "}") { (key, value) -> """"$key": $value""" }

fun String.toJsQuote(): String {
    val product = StringBuilder()
    product.append("\"")
    val chars = this.toCharArray()
    chars.forEach {
        when (it) {
            '\b' -> product.append("\\b")
            '\t' -> product.append("\\t")
            '\n' -> product.append("\\n")
            '\r' -> product.append("\\r")
            '"' -> product.append("\\\"")
            '\\' -> product.append("\\\\")
            else -> if (it.toInt() < 32) {
                product.append(unicodeEscape(it))
            } else {
                product.append(it)
            }
        }
    }
    product.append("\"")
    return product.toString()
}

fun unicodeEscape(ch: Char): String {
    val sb = StringBuilder()
    sb.append("\\u")
    val hex = Integer.toHexString(ch.toInt())
    for (i in hex.length..3) {
        sb.append('0')
    }
    sb.append(hex)
    return sb.toString()
}
