package ac.uk.ebi.biostd.tsv.serialization

import ebi.ac.uk.base.EMPTY

internal const val TITLE_KEY = "Title"
internal const val ACC_NO_KEY = "Submission"
internal const val ROOT_PATH_KEY = "RootPath"
internal val specialCharRegex = "[\n\t\"]".toRegex()

internal class TsvBuilder(
    private val builder: StringBuilder,
) {
    constructor() : this(StringBuilder())

    override fun toString(): String = builder.toString()

    fun with(
        key: String,
        value: String?,
    ) {
        val result =
            when {
                value == null -> EMPTY
                value.contains(specialCharRegex) -> "\"$value\""
                else -> value
            }
        builder.append("$key\t${result}\n")
    }

    fun append(value: String) {
        builder.append(value)
    }

    fun addTableRow(headers: List<String>) {
        append(headers.joinToString(separator = "\t"))
        append("\n")
    }
}
