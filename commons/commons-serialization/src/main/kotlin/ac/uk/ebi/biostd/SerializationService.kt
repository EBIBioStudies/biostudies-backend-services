package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Submission

class SerializationService(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val tsvSerializer: TsvSerializer = TsvSerializer()
) {

    fun serializeSubmission(submission: Submission, format: SubFormat): String {
        return when (format) {
            SubFormat.XML ->
                xmlSerializer.serialize(submission)
            SubFormat.JSON ->
                jsonSerializer.serialize(submission)
            SubFormat.TSV ->
                tsvSerializer.serialize(submission)
        }
    }

    fun deserializeSubmission(submission: String, format: SubFormat): Submission {
        return when (format) {
            SubFormat.XML ->
                xmlSerializer.deserialize(submission)
            SubFormat.JSON ->
                jsonSerializer.deserialize(submission)
            SubFormat.TSV ->
                tsvSerializer.deserialize(submission)
        }
    }

    fun deserializeList(submission: String, format: SubFormat): List<Submission> {
        return when (format) {
            SubFormat.XML ->
                throw NotImplementedError()
            SubFormat.JSON ->
                jsonSerializer.deserialize(submission)
            SubFormat.TSV ->
                tsvSerializer.deserializeList(submission)
        }
    }
}

enum class SubFormat {
    XML, JSON, TSV
}
