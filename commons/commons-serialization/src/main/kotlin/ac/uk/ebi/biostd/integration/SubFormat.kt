package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.exception.InvalidExtensionException
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import java.io.File

sealed class SubFormat(private val description: String) {
    companion object {
        fun fromFile(file: File): SubFormat {
            return when (file.extension) {
                "tsv" -> TsvFormat.Tsv
                "json" -> PlainJson
                else -> throw InvalidExtensionException(file.name)
            }
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
