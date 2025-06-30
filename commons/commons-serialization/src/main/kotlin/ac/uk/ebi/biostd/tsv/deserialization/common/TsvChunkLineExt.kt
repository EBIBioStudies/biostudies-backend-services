package ac.uk.ebi.biostd.tsv.deserialization.common

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine

internal fun TsvChunkLine.name(): String? {
    val chunkName = name
    return when {
        chunkName == null -> null
        (isReference(chunkName) || isNameDetail(chunkName) || isValueDetail(chunkName)).not() -> chunkName
        else -> chunkName.substring(1, chunkName.lastIndex)
    }
}

internal fun isNameDetail(attributeName: String): Boolean = attributeName.matches("\\(.+\\)".toRegex())

internal fun isValueDetail(attributeName: String): Boolean = attributeName.matches("\\[.+\\]".toRegex())

internal fun isReference(attributeName: String): Boolean = attributeName.matches("<.+>".toRegex())
