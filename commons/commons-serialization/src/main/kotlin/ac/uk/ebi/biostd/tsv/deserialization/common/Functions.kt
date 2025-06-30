package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_NAME
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_VAL
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ac.uk.ebi.biostd.validation.REQUIRED_TABLE_ROWS
import ac.uk.ebi.biostd.validation.TABLE_HEADER_CAN_NOT_BE_BLANK
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.util.collections.trimTrailingIndexedWhile
import ebi.ac.uk.util.collections.trimTrailingWhile

internal inline fun validate(
    value: Boolean,
    lazyMessage: () -> String,
) {
    if (!value) {
        throw InvalidElementException(lazyMessage())
    }
}

internal fun toAttributes(chunkLines: List<TsvChunkLine>): List<Attribute> {
    val attributes = chunkLines.map { (it.name ?: throw InvalidElementException(REQUIRED_ATTR_NAME)) to it.value }
    return getTableAttributes(attributes)
}

internal fun <T> asTable(
    chunk: TsvChunk,
    initializer: (String?, List<Attribute>) -> T,
): List<T> {
    val rows =
        buildList {
            chunk.lines.ifEmpty { throw InvalidElementException(REQUIRED_TABLE_ROWS) }
            chunk.lines.forEach { line ->
                val attrs = getTableAttributes(line, chunk)
                add(initializer(line.name(), attrs))
            }
        }

    return rows
}

private fun getTableAttributes(
    line: TsvChunkLine,
    chunk: TsvChunk,
): List<Attribute> {
    val headers = chunk.header.rawValues.trimTrailingWhile { it == null }
    val lineValues = line.rawValues.trimTrailingIndexedWhile { idx, value -> value == null && idx > headers.lastIndex }

    validate(lineValues.size <= headers.size) { throw InvalidElementException(INVALID_TABLE_ROW) }
    validate(headers.all { it.isNotBlank() }) { throw InvalidElementException(TABLE_HEADER_CAN_NOT_BE_BLANK) }

    val values = headers.mapIndexed { i, value -> value!! to lineValues.getOrNull(i) }
    return getTableAttributes(values)
}

private fun getTableAttributes(values: List<Pair<String, String?>>): List<Attribute> =
    buildList {
        var previous: Attribute? = null
        for ((header, value) in values) {
            require(header.isNotBlank()) { throw InvalidElementException(REQUIRED_ATTR_NAME) }
            when {
                isNameDetail(header) -> {
                    if (previous == null) throw InvalidElementException(MISPLACED_ATTR_NAME)
                    previous.nameAttrs.add(AttributeDetail(getDetailName(header), value))
                }

                isValueDetail(header) -> {
                    if (previous == null) throw InvalidElementException(MISPLACED_ATTR_VAL)
                    previous.valueAttrs.add(AttributeDetail(getDetailName(header), value))
                }

                isReference(header) -> {
                    previous = Attribute(getDetailName(header), value, reference = true)
                    add(previous)
                }

                else -> {
                    previous = Attribute(header, value)
                    add(previous)
                }
            }
        }
    }

private fun getDetailName(attrName: String) = attrName.substring(1, attrName.lastIndex)
