package ac.uk.ebi.biostd.serialization.tsv

class Tsv(val lines: MutableList<TsvLine> = mutableListOf()) {

    override fun toString(): String {
        return lines.map { it.values.joinToString(separator = "\t") }.joinToString(separator = "\n")
    }
}

class TsvLine(val values: MutableList<String> = mutableListOf())

fun tsv(function: Tsv.() -> Unit): Tsv = Tsv().apply(function)

fun Tsv.line(vararg tabValues: String) = lines.add(TsvLine().apply { values.addAll(tabValues) })