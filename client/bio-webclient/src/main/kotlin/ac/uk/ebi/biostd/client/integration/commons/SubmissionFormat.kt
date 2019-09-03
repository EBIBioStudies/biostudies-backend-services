package ac.uk.ebi.biostd.client.integration.commons

import ac.uk.ebi.biostd.integration.SubFormat
import org.springframework.http.MediaType

enum class SubmissionFormat(val mediaType: MediaType, val submissionType: MediaType = mediaType) {
    JSON(MediaType.APPLICATION_JSON),
    TSV(MediaType.TEXT_PLAIN),
    XML(MediaType.TEXT_XML);

    companion object {
        fun fromSubFormat(subFormat: SubFormat) = when (subFormat) {
            SubFormat.TSV -> TSV
            SubFormat.XML -> XML
            SubFormat.JSON -> JSON
            SubFormat.JSON_PRETTY -> JSON
        }
    }

    fun asSubFormat() = when (this) {
        JSON -> SubFormat.JSON
        TSV -> SubFormat.TSV
        XML -> SubFormat.XML
    }
}
