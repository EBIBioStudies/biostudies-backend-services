package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.INVALID_TABLE_ROW
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_NAME
import ac.uk.ebi.biostd.validation.MISPLACED_ATTR_VAL
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
        when {
            line.isNameDetail() -> addNameAttributeDetail(line.name(), line.value, attributes)
            line.isValueDetail() -> addValueAttributeDetail(line.name(), line.value, attributes)
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
            attr.applyIfNotBlank { parseTableAttribute(chunk.header[index + 1], attr, attrs) }
        }

        rows.add(initializer(it.name(), attrs))
    }

    return rows.toList()
}

private fun parseTableAttribute(name: String, value: String, attributes: MutableList<Attribute>) {
    when {
        isNameDetail(name) -> addNameAttributeDetail(getDetailName(name), value, attributes)
        isValueDetail(name) -> addValueAttributeDetail(getDetailName(name), value, attributes)
        else -> attributes.add(Attribute(name, value))
    }
}

private fun getDetailName(attrName: String) = attrName.substring(1, attrName.lastIndex)

private fun addNameAttributeDetail(name: String, value: String, attributes: MutableList<Attribute>) {
    attributes.ifEmpty { throw InvalidElementException(MISPLACED_ATTR_NAME) }
    attributes.last().nameAttrs.add(AttributeDetail(name, value))
}

private fun addValueAttributeDetail(name: String, value: String, attributes: MutableList<Attribute>) {
    attributes.ifEmpty { throw InvalidElementException(MISPLACED_ATTR_VAL) }
    attributes.last().valueAttrs.add(AttributeDetail(name, value))
}
