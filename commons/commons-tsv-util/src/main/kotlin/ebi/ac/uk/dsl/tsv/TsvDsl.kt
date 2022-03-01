package ebi.ac.uk.dsl.tsv

class Tsv(val lines: MutableList<TsvLine> = mutableListOf()) {

    override fun toString() = lines.joinToString(separator = "\n") { it.values.joinToString(separator = "\t") }
}

class TsvLine(val values: MutableList<String> = mutableListOf())

fun tsv(function: Tsv.() -> Unit): Tsv = Tsv().apply(function)

fun Tsv.line(vararg tabValues: Any = emptyArray()): Boolean {
    return lines.add(TsvLine(tabValues.mapTo(mutableListOf(), Any::toString)))
}
