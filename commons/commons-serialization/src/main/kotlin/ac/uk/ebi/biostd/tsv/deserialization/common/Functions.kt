package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_NAME
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_VAL
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_VALUE
import ac.uk.ebi.biostd.validation.REQUIRED_TABLE_ROWS
import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

internal inline fun validate(value: Boolean, lazyMessage: () -> String) {
    if (!value) {
        throw InvalidElementException(lazyMessage())
    }
}

internal fun toAttributes(chunkLines: List<TsvChunkLine>): MutableList<Attribute> {
    val attributes: MutableList<Attribute> = mutableListOf()
    chunkLines.forEach { line ->
        line.value.ifBlank { throw InvalidElementException(REQUIRED_ATTR_VALUE) }
        when {
            line.isNameDetail() -> addNameAttributeDetail(attributes, line)
            line.isValueDetail() -> addValueAttributeDetail(attributes, line)
            else -> attributes.add(Attribute(line.name(), line.value, line.isReference()))
        }
    }
    return attributes
}

internal fun <T> asTable(chunk: TsvChunk, initializer: (String, MutableList<Attribute>) -> T): List<T> {
    val rows: MutableList<T> = mutableListOf()

    chunk.lines.ifEmpty { throw InvalidElementException(REQUIRED_TABLE_ROWS) }
    chunk.lines.forEach {
        val attrs: MutableList<Attribute> = mutableListOf()
        val rowAttrsSize = it.rawValues.size
        val headerAttrsSize = chunk.header.size - 1

        validate(rowAttrsSize <= headerAttrsSize) { throw InvalidElementException(INVALID_TABLE_ROW) }

        it.rawValues.forEachIndexed { index, attr ->
            attr.applyIfNotBlank { attrs.add(Attribute(chunk.header[index + 1], attr)) }
        }

        rows.add(initializer(it.name(), attrs))
    }

    return rows.toList()
}

private fun addNameAttributeDetail(attributes: MutableList<Attribute>, line: TsvChunkLine) {
    attributes.ifEmpty { throw InvalidElementException(MISPLACED_ATTR_NAME) }
    attributes.last().nameAttrs.add(AttributeDetail(line.name(), line.value))
}

private fun addValueAttributeDetail(attributes: MutableList<Attribute>, line: TsvChunkLine) {
    attributes.ifEmpty { throw InvalidElementException(MISPLACED_ATTR_VAL) }
    attributes.last().valueAttrs.add(AttributeDetail(line.name(), line.value))
}
