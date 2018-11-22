package ebi.ac.uk.dsl

class Tsv(val lines: MutableList<TsvLine> = mutableListOf()) {

    override fun toString() =
        lines.joinToString(separator = "\n") { it.values.joinToString(separator = "\t") }
}

class TsvLine(val values: MutableList<String> = mutableListOf())

fun tsv(function: Tsv.() -> Unit): Tsv = Tsv().apply(function)

fun Tsv.line(vararg tabValues: Any = emptyArray()) =
    lines.add(TsvLine().apply { values.addAll(tabValues.mapTo(mutableListOf(), Any::toString)) })
