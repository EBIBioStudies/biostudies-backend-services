package ac.uk.ebi.biostd.client.integration.commons

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.model.constants.APPLICATION_XLSX
import org.springframework.http.MediaType

enum class SubmissionFormat(val mediaType: MediaType, val submissionType: MediaType = mediaType) {
    JSON(MediaType.APPLICATION_JSON),
    TSV(MediaType.TEXT_PLAIN),
    XLSX(MediaType.TEXT_PLAIN, MediaType.valueOf(APPLICATION_XLSX)),
    XML(MediaType.TEXT_XML);

    fun asSubFormat(): SubFormat {
        return when (this) {
            JSON -> SubFormat.JSON
            TSV -> SubFormat.TSV
            XLSX -> SubFormat.TSV
            XML -> SubFormat.XML
        }
    }
}
