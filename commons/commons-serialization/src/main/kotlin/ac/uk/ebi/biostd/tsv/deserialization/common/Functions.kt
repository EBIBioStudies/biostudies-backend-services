package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_NAME
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_VAL
import ac.uk.ebi.biostd.validation.REQUIRED_TABLE_ROWS
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

internal inline fun validate(value: Boolean, lazyMessage: () -> String) {
    if (!value) {
        throw InvalidElementException(lazyMessage())
    }
}

internal fun toAttributes(chunkLines: List<TsvChunkLine>): List<Attribute> =
    getAttributes(chunkLines.map { it.name to it.value.nullIfBlank() })

internal fun <T> asTable(chunk: TsvChunk, initializer: (String, List<Attribute>) -> T): List<T> {
    val rows = buildList {
        chunk.lines.ifEmpty { throw InvalidElementException(REQUIRED_TABLE_ROWS) }
        chunk.lines.forEach { line ->
            val attrs = getAttributes(line, chunk)
            add(initializer(line.name(), attrs))
        }
    }

    return rows
}

private fun getAttributes(line: TsvChunkLine, chunk: TsvChunk): List<Attribute> {
    validate(line.size <= chunk.header.size) { throw InvalidElementException(INVALID_TABLE_ROW) }
    val values = chunk.header.rawValues.mapIndexed { i, value -> value to line.rawValues.getOrNull(i).nullIfBlank() }
    return getAttributes(values)
}

private fun getAttributes(values: List<Pair<String, String?>>): List<Attribute> = buildList {
    var previous: Attribute? = null
    for ((header, value) in values) {
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
