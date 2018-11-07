package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_SEPARATOR

data class TsvChunkLine(private val name: String, val value: String) {
    fun isReference() = name.matches("<.+>".toRegex())
    fun isNameDetail() = name.matches("\\(.+\\)".toRegex())
    fun isValueDetail() = name.matches("\\[.+\\]".toRegex())
    fun name() = if (!isReference() && !isNameDetail() && !isValueDetail()) name else name.substring(1, name.lastIndex)
}

fun MutableList<TsvChunkLine>.hasTableLines() = size > 1 && first().value.contains(TSV_SEPARATOR)
