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

// TODO: add proper exception
internal fun toAttributes(chunkLines: List<TsvChunkLine>): MutableList<Attribute> {
    val attributes: MutableList<Attribute> = mutableListOf()
    chunkLines.forEach { line ->
        when {
            line.isNameDetail() -> addNameAttributeDetail(line.name(), line.value!!, attributes)
            line.isValueDetail() -> addValueAttributeDetail(line.name(), line.value!!, attributes)
            else -> attributes.add(Attribute(line.name(), line.value.nullIfBlank(), line.isReference()))
        }
    }
    return attributes
}

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

private fun getAttributes(line: TsvChunkLine, chunk: TsvChunk): List<Attribute> = buildList {
    val rowAttrsSize = line.rawValues.size
    val headerAttrsSize = chunk.header.size - 1

    validate(rowAttrsSize <= headerAttrsSize) { throw InvalidElementException(INVALID_TABLE_ROW) }

    chunk.header.rawValues.forEachIndexed { index, headerName ->
        parseTableAttribute(headerName, line.rawValues.getOrNull(index).nullIfBlank())
    }
}

private fun MutableList<Attribute>.parseTableAttribute(headerName: String, value: String?) {
    when {
        isNameDetail(headerName) -> {
            if (value == null) throw IllegalArgumentException("NameDetail value must be not null")
            addNameAttributeDetail(getDetailName(headerName), value, this)
        }
        isValueDetail(headerName) -> {
            if (value == null) throw IllegalArgumentException("ValueDetail value must be not null")
            addValueAttributeDetail(getDetailName(headerName), value, this)
        }
        else -> add(Attribute(headerName, value))
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
