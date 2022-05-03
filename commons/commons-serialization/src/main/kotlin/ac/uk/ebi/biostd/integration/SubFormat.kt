package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.exception.InvalidExtensionException
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import java.io.File

sealed class SubFormat(private val description: String) {
    companion object {
        fun valueOf(format: String): SubFormat {
            return when (format) {
                "TSV" -> TSV
                "XML" -> XML
                "JSON" -> JSON
                else -> throw InvalidExtensionException(format)
            }
        }

        fun fromFile(file: File): SubFormat {
            return when (file.extension) {
                "tsv" -> TsvFormat.Tsv
                "xml" -> XmlFormat
                "json" -> PlainJson
                else -> throw InvalidExtensionException(file.name)
            }
        }

        val TSV: TsvFormat get() = TsvFormat.Tsv
        val XML: XmlFormat get() = XmlFormat
        val JSON_PRETTY: JsonPretty get() = JsonPretty
        val JSON: PlainJson get() = PlainJson
    }

    override fun toString(): String = description

    object XmlFormat : SubFormat("XML")

    sealed class TsvFormat : SubFormat("TSV") {
        object Tsv : TsvFormat()
    }

    sealed class JsonFormat : SubFormat("JSON") {
        object PlainJson : JsonFormat()
        object JsonPretty : JsonFormat()
    }
}
