package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.InvalidElementException
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail

internal inline fun validate(value: Boolean, lazyMessage: () -> String) {
    if (!value) {
        throw InvalidElementException(lazyMessage())
    }
}

// TODO add proper error handling
// - if name detail is first
// - if not detail value
internal fun toAttributes(chunkLines: List<TsvChunkLine>): MutableList<Attribute> {
    val attributes: MutableList<Attribute> = mutableListOf()
    chunkLines.forEach { line ->
        when {
            line.isNameDetail() -> attributes.last().nameAttrs.add(AttributeDetail(line.name(), line.value))
            line.isValueDetail() -> attributes.last().valueAttrs.add(AttributeDetail(line.name(), line.value))
            else -> attributes.add(Attribute(line.name(), line.value, line.isReference()))
        }
    }
    return attributes
}

// TODO add proper error handling
internal fun <T> asTable(chunk: TsvChunk, initializer: (String, MutableList<Attribute>) -> T): List<T> {
    val rows: MutableList<T> = mutableListOf()

    chunk.lines.forEach {
        val attrs: MutableList<Attribute> = mutableListOf()
        it.values.forEachIndexed { index, attr -> attrs.add(Attribute(chunk.header[index + 1], attr)) }
        rows.add(initializer(it.name(), attrs))
    }

    return rows.toList()
}
