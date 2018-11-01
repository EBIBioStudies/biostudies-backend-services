package ac.uk.ebi.biostd.serialization.tsv

data class TsvChunkLine(val name: String, val value: String) {
    fun isReference(): Boolean = name[0] == '<'
    fun isNameDetail(): Boolean = name[0] == '('
    fun isValueDetail(): Boolean = name[0] == '['
    fun getTrimmedName(): String = name.substring(1, name.length - 1)
}
