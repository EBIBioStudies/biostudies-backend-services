package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine

internal fun TsvChunkLine.isReference() = name.matches("<.+>".toRegex())
internal fun TsvChunkLine.isNameDetail() = isNameDetail(name)
internal fun TsvChunkLine.isValueDetail() = isValueDetail(name)
internal fun TsvChunkLine.name() =
    if ((isReference() || isNameDetail() || isValueDetail()).not()) name else name.substring(1, name.lastIndex)

internal fun isNameDetail(attributeName: String) = attributeName.matches("\\(.+\\)".toRegex())

internal fun isValueDetail(attributeName: String) = attributeName.matches("\\[.+\\]".toRegex())
