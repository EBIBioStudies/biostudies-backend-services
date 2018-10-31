package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_SEPARATOR
import ebi.ac.uk.model.Attribute

data class TsvChunk(
        val header: List<String>,
        val lines: MutableList<TsvChunkLine>) {

    constructor(body: MutableList<String>) : this(body.removeAt(0).split(TSV_SEPARATOR), mutableListOf()) {
        body.forEach {
            lines.add(TsvChunkLine(it.substringBefore(TSV_SEPARATOR), it.substringAfter(TSV_SEPARATOR)))
        }
    }

    fun getType(): String = header[0]

    fun getIdentifier(): String = header[1]

    fun <T> mapTable(initializer: (String, MutableList<Attribute>) -> T): List<T> {
        val rows: MutableList<T> = mutableListOf()

        lines.forEach {
            val attrs: MutableList<Attribute> = mutableListOf()
            it.value.split(TSV_SEPARATOR).forEachIndexed { index, attr -> attrs.add(Attribute(header[index + 1], attr)) }
            rows.add(initializer(it.name, attrs))
        }

        return rows.toList()
    }
}
