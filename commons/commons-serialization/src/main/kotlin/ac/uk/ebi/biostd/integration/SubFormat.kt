package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import java.io.File

sealed class SubFormat(
    private val description: String,
) {
    companion object {
        fun fromFile(file: File): SubFormat = fromString(file.extension)

        fun fromString(format: String): SubFormat =
            when (format.lowercase()) {
                "tsv" -> TsvFormat.Tsv
                "json" -> PlainJson
                else -> error("Unsupported format: $format")
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
