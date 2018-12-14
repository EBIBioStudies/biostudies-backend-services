package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine

fun TsvChunkLine.isReference() = name.matches("<.+>".toRegex())
fun TsvChunkLine.isNameDetail() = name.matches("\\(.+\\)".toRegex())
fun TsvChunkLine.isValueDetail() = name.matches("\\[.+\\]".toRegex())
fun TsvChunkLine.name() = if (!isReference() && !isNameDetail() && !isValueDetail()) name else name.substring(1, name.lastIndex)
