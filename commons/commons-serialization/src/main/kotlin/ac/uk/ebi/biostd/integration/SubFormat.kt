package ac.uk.ebi.biostd.integration

import ac.uk.ebi.biostd.exception.InvalidExtensionException

sealed class SubFormat {

    companion object {
        fun valueOf(format: String): SubFormat {
            return when (format) {
                "TSV" -> PlainTsv
                "XML" -> XmlFormat
                "JSON" -> PlainJson
                else -> throw InvalidExtensionException(format)
            }
        }

        fun fromExtension(extension: String): SubFormat {
            return when (extension) {
                "tsv" -> PlainTsv
                "xlsx" -> XlsxTsv
                "xml" -> XmlFormat
                "json" -> PlainJson
                else -> throw InvalidExtensionException(extension)
            }
        }

        val TSV = PlainTsv
        val XML = XmlFormat
        val JSON_PRETTY = PrettyJson
        val JSON = PlainJson
    }
}

object XmlFormat : SubFormat()

sealed class JsonFormat : SubFormat()
object PlainJson : JsonFormat()
object PrettyJson : JsonFormat()

sealed class TsvFormat : SubFormat()
object PlainTsv : TsvFormat()
object XlsxTsv : TsvFormat()
