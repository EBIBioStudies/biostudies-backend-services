package ac.uk.ebi.biostd.serialization

import ac.uk.ebi.biostd.mapping.SubmissionMapper
import ac.uk.ebi.biostd.serialization.json.JsonSerializer
import ac.uk.ebi.biostd.serialization.tsv.TsvSerializer
import ac.uk.ebi.biostd.serialization.xml.XmlSerializer
import ebi.ac.uk.model.ISubmission

class SerializationService(
        private val jsonSerializer: JsonSerializer,
        private val xmlSerializer: XmlSerializer,
        private val tsvSerializer: TsvSerializer,
        private val submissionMapper: SubmissionMapper) {

    fun serializeSubmission(submission: ISubmission, outputFormat: SubFormat): String {
        return when (outputFormat) {
            SubFormat.XML ->
                xmlSerializer.serialize(asSubmission(submission))
            SubFormat.JSON ->
                jsonSerializer.serialize(asSubmission(submission))
            SubFormat.TSV ->
                tsvSerializer.serialize(asSubmission(submission))
        }
    }

    private fun asSubmission(submission: ISubmission) = submissionMapper.mapSubmission(submission)

}

enum class SubFormat {
    XML, JSON, TSV
}