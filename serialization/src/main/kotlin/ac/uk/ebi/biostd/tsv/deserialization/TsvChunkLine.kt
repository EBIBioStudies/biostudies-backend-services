package ac.uk.ebi.biostd.tsv.deserialization

data class TsvChunkLine(private val name: String, val value: String) {
    fun isReference() = name.matches("<.+>".toRegex())
    fun isNameDetail() = name.matches("\\(.+\\)".toRegex())
    fun isValueDetail() = name.matches("\\[.+\\]".toRegex())
    fun name() = if (!isReference() && !isNameDetail() && !isValueDetail()) name else name.substring(1, name.lastIndex)
}
