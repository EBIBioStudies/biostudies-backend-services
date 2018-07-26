package ac.uk.ebi.biostd.serialization.tsv

internal const val titleKey = "Title"
internal const val releaseDateKey = "ReleaseDate"
internal const val accNoKey = "Submission"
internal const val linkKey = "Link"
internal const val rootPathKey = "RootPath"
internal const val linkTableUrlHeader = "Links"

internal const val attrValSeparator = ";"
internal const val tagsSeparator = attrValSeparator

class TsvBuilder(private val builder: StringBuilder) {

    constructor() : this(StringBuilder())

    override fun toString(): String = builder.toString()

    fun with(key: String, value: String) {
        builder.append("$key\t$value\n")
    }

    fun append(value: String) {
        builder.append(value)
    }

    fun addTableHeaders(headers: Set<String>) {
        builder.append(headers.joinToString(separator = "\t"))
        builder.append("\n")
    }

    fun addTableValue(tableValue: String?) {
        builder.append("${tableValue.orEmpty()}\t")
    }
}
