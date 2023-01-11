package ebi.ac.uk.base

/**
 * Rewrites a string to pretty considering custom indentation size. The default identation size is 2.
 */
fun Any.toPrettyString(indentSize: Int = 2) = " ".repeat(indentSize).let { indent ->
    toString()
        .replace(", ", ",\n$indent")
        .replace("(", "(\n$indent")
        .dropLast(1) + "\n)"
}
