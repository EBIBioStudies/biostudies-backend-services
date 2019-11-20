package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.exception.InvalidExtensionException

sealed class SubFormat {
    companion object {
        fun valueOf(format: String): SubFormat {
            return when (format) {
                "TSV" -> Tsv
                "XML" -> XmlFormat
                "JSON" -> PlainJson
                else -> throw InvalidExtensionException(format)
            }
        }

        fun fromExtension(extension: String): SubFormat {
            return when (extension) {
                "tsv" -> Tsv
                "xlsx" -> XlsxTsv
                "xml" -> XmlFormat
                "json" -> PlainJson
                else -> throw InvalidExtensionException(extension)
            }
        }

        val TSV = Tsv
        val XML = XmlFormat
        val JSON_PRETTY = JsonPretty
        val JSON = PlainJson
    }
}

object XmlFormat : SubFormat()

sealed class JsonFormat : SubFormat()
object PlainJson : JsonFormat()
object JsonPretty : JsonFormat()

sealed class TsvFormat : SubFormat()
object Tsv : TsvFormat()
object XlsxTsv : TsvFormat()
