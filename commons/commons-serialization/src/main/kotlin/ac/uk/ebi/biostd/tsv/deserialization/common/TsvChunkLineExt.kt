package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine

internal fun TsvChunkLine.isReference() = name.matches("<.+>".toRegex())
internal fun TsvChunkLine.isNameDetail() = name.matches("\\(.+\\)".toRegex())
internal fun TsvChunkLine.isValueDetail() = name.matches("\\[.+\\]".toRegex())
internal fun TsvChunkLine.name() =
    if ((isReference() || isNameDetail() || isValueDetail()).not()) name else name.substring(1, name.lastIndex)
