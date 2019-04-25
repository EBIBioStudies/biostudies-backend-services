package ac.uk.ebi.biostd.client.integration.commons

import ac.uk.ebi.biostd.SubFormat
import org.springframework.http.MediaType

enum class SubmissionFormat(val mediaType: MediaType) {
    JSON(MediaType.APPLICATION_JSON),
    TSV(MediaType.TEXT_PLAIN),
    XML(MediaType.TEXT_XML);

    fun asSubFormat(): SubFormat {
        return when (this) {
            JSON -> SubFormat.JSON
            TSV -> SubFormat.TSV
            XML -> SubFormat.XML
        }
    }
}
