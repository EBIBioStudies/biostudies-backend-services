package ac.uk.ebi.biostd.client.integration.commons

import ac.uk.ebi.biostd.integration.JsonFormat
import ac.uk.ebi.biostd.integration.PlainTsv
import ac.uk.ebi.biostd.integration.PrettyJson
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.TsvFormat
import ac.uk.ebi.biostd.integration.XmlFormat
import org.springframework.http.MediaType

enum class SubmissionFormat(val mediaType: MediaType, val submissionType: MediaType = mediaType) {
    JSON(MediaType.APPLICATION_JSON),
    TSV(MediaType.TEXT_PLAIN),
    XML(MediaType.TEXT_XML);

    companion object {
        fun fromSubFormat(subFormat: SubFormat) = when (subFormat) {
            XmlFormat -> XML
            is JsonFormat -> JSON
            is TsvFormat -> TSV
        }
    }

    fun asSubFormat() = when (this) {
        JSON -> PrettyJson
        TSV -> PlainTsv
        XML -> XmlFormat
    }
}
