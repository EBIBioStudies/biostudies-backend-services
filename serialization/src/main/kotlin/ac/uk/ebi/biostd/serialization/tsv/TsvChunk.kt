package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_SEPARATOR
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.util.collections.second

data class TsvChunk(
        val header: List<String>,
        val lines: MutableList<TsvChunkLine>) {

    constructor(body: MutableList<String>) : this(body.removeAt(0).split(TSV_SEPARATOR), mutableListOf()) {
        body.forEach {
            lines.add(TsvChunkLine(it.substringBefore(TSV_SEPARATOR), it.substringAfter(TSV_SEPARATOR)))
        }
    }

    fun getType() = header.first()

    fun getIdentifier() = if (header.size > 1) header.second() else EMPTY

    fun getParent() = if (header.size > 2) header[2] else EMPTY

    fun isTableChunk() = header.size > 1 && lines.hasTableLines()

    /**
     * Since a sections table and a subsection chunk may have exactly the same header, this is how to differentiate them:
     * 1) A subsection chunk MUST have exactly 3 values in its header: the type, the acc No and the parent acc No
     * 2) A subsection chunk may not have lines but, if it has, they should NOT be table lines
     */
    fun isSubsectionChunk() = header.size == 3 && (lines.isEmpty() || lines.hasTableLines().not())

    fun <T> mapTable(initializer: (String, MutableList<Attribute>) -> T): List<T> {
        val rows: MutableList<T> = mutableListOf()

        lines.forEach {
            val attrs: MutableList<Attribute> = mutableListOf()
            it.value.split(TSV_SEPARATOR).forEachIndexed { index, attr -> attrs.add(Attribute(header[index + 1], attr)) }
            rows.add(initializer(it.name(), attrs))
        }

        return rows.toList()
    }
}
