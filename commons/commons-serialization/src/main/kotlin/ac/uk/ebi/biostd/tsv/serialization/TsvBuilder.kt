package ac.uk.ebi.biostd.tsv.serialization

internal const val TITLE_KEY = "Title"
internal const val ACC_NO_KEY = "Submission"
internal const val ROOT_PATH_KEY = "RootPath"

internal const val ATTR_VAL_SEPARATOR = ";"
internal const val TAGS_SEPARATOR = ATTR_VAL_SEPARATOR

internal class TsvBuilder(private val builder: StringBuilder) {

    constructor() : this(StringBuilder())

    override fun toString(): String = builder.toString()

    fun with(key: String, value: String) {
        builder.append("$key\t$value\n")
    }

    fun append(value: String) {
        builder.append(value)
    }

    fun addTableRow(headers: List<String>) {
        append(headers.joinToString(separator = "\t"))
        append("\n")
    }
}
