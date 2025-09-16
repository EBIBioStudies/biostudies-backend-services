package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.exception.InvalidFormatException
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import java.io.File

sealed class SubFormat(
    private val description: String,
) {
    companion object {
        const val JSON_EXTENSION = "json"
        const val TSV_EXTENSION = "tsv"
        const val XLSX_EXTENSION = "xlsx"

        fun checkFileListExtension(fileName: String) {
            val extension = fileName.substringAfterLast(".")
            require(extension == JSON_EXTENSION || extension == TSV_EXTENSION || extension == XLSX_EXTENSION) {
                throw InvalidFormatException(extension)
            }
        }

        fun fromFile(file: File): SubFormat = fromString(file.extension)

        fun fromString(format: String): SubFormat =
            when (format.lowercase()) {
                TSV_EXTENSION -> TsvFormat.Tsv
                JSON_EXTENSION -> PlainJson
                else -> throw InvalidFormatException(format)
            }

        val TSV: TsvFormat get() = TsvFormat.Tsv
        val JSON_PRETTY: JsonPretty get() = JsonPretty
        val JSON: PlainJson get() = PlainJson
    }

    override fun toString(): String = description

    sealed class TsvFormat : SubFormat("TSV") {
        object Tsv : TsvFormat()
    }

    sealed class JsonFormat : SubFormat("JSON") {
        object PlainJson : JsonFormat()

        object JsonPretty : JsonFormat()
    }
}
