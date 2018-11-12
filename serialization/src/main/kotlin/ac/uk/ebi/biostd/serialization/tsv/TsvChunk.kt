package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.SECTION_TABLE_CL
import ac.uk.ebi.biostd.serialization.common.SECTION_TABLE_OP
import ac.uk.ebi.biostd.serialization.common.TSV_SEPARATOR
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.util.collections.secondOrElse
import ebi.ac.uk.util.collections.thirdOrElse

data class TsvChunk(
    val header: List<String>,
    val lines: MutableList<TsvChunkLine>
) {

    constructor(body: MutableList<String>) : this(body.removeAt(0).split(TSV_SEPARATOR), mutableListOf()) {
        body.forEach {
            lines.add(TsvChunkLine(it.substringBefore(TSV_SEPARATOR), it.substringAfter(TSV_SEPARATOR)))
        }
    }

    fun getType() = if (isSectionTable()) header.first().substringBefore(SECTION_TABLE_OP) else header.first()

    fun getIdentifier() = header.secondOrElse(EMPTY)

    fun getParent() =
            if (isSectionTable()) header.first().substringAfter(SECTION_TABLE_OP).substringBefore(SECTION_TABLE_CL)
            else header.thirdOrElse(EMPTY)

    fun isSubsection() = getParent().isNotEmpty()

    fun isSectionTable() = header.first().matches(".+\\[.+|\\]".toRegex())

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
