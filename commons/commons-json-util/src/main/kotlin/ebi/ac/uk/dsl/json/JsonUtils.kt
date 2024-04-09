package ebi.ac.uk.dsl.json

fun String.toJsonQuote(): String {
    val product = StringBuilder()
    toCharArray().forEach { product.append(scape(it)) }
    return "\"$product\""
}

fun scape(char: Char) =
    when (char) {
        '\t' -> "\\t"
        '\b' -> "\\b"
        '\n' -> "\\n"
        '\r' -> "\\r"
        '\'' -> "\\'"
        '"' -> "\\\""
        '\\' -> "\\\\"
        else -> char.toString()
    }
