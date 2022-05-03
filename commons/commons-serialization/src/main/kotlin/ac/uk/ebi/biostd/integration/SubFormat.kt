package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.exception.InvalidExtensionException
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.PlainJson
import java.io.File

sealed class SubFormat {
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

    object XmlFormat : SubFormat()

    sealed class TsvFormat : SubFormat() {
        object Tsv : TsvFormat()
        // object XlsxTsv : TsvFormat()
    }

    sealed class JsonFormat : SubFormat() {
        object PlainJson : JsonFormat()
        object JsonPretty : JsonFormat()
    }
}
