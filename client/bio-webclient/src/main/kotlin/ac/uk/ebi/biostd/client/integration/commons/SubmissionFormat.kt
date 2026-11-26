package ac.uk.ebi.biostd.client.integration.commons

import org.springframework.http.MediaType
import uk.ac.ebi.biostd.integration.SubFormat.JsonFormat.JsonPretty
import uk.ac.ebi.biostd.integration.SubFormat.TsvFormat.Tsv

enum class SubmissionFormat(val mediaType: MediaType, val submissionType: MediaType = mediaType) {
    JSON(MediaType.APPLICATION_JSON),
    TSV(MediaType.TEXT_PLAIN),
    ;

    fun asSubFormat() =
        when (this) {
            JSON -> JsonPretty
            TSV -> Tsv
        }
}
