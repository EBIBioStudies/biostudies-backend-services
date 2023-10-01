package ac.uk.ebi.biostd.client.integration.commons

import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import org.springframework.http.MediaType

enum class SubmissionFormat(val mediaType: MediaType, val submissionType: MediaType = mediaType) {
    JSON(MediaType.APPLICATION_JSON),
    TSV(MediaType.TEXT_PLAIN),
    XML(MediaType.TEXT_XML);

    fun asSubFormat() = when (this) {
        JSON -> JsonPretty
        TSV -> Tsv
        XML -> XmlFormat
    }
}
