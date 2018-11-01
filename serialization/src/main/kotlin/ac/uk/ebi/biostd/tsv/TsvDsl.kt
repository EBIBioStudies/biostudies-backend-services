package ac.uk.ebi.biostd.tsv

class Tsv(val lines: MutableList<TsvLine> = mutableListOf()) {

    override fun toString(): String {
        return lines.map { it.values.joinToString(separator = "\t") }.joinToString(separator = "\n")
    }
}

class TsvLine(val values: MutableList<String> = mutableListOf())

fun tsv(function: Tsv.() -> Unit): Tsv = Tsv().apply(function)
fun Tsv.line(vararg tabValues: Any = emptyArray()) = lines.add(TsvLine().apply { values.addAll(tabValues.mapTo(mutableListOf(), Any::toString)) })

