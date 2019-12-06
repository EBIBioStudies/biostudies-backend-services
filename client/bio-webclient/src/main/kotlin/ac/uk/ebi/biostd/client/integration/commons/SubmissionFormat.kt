package ac.uk.ebi.biostd.client.integration.commons

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
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
        JSON -> JsonPretty
        TSV -> Tsv
        XML -> XmlFormat
    }
}
