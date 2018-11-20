package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.serialization.tsv.TsvDeserializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Submission

class SerializationService(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val tsvSerializer: TsvSerializer = TsvSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer()
) {

    fun serializeSubmission(submission: Submission, outputFormat: SubFormat): String {
        return when (outputFormat) {
            SubFormat.XML ->
                xmlSerializer.serialize(submission)
            SubFormat.JSON ->
                jsonSerializer.serialize(submission)
            SubFormat.TSV ->
                tsvSerializer.serialize(submission)
        }
    }

    fun deserializeSubmission(submission: String, outputFormat: SubFormat): Submission {
        return when (outputFormat) {
            SubFormat.XML ->
                xmlSerializer.deserialize(submission, Submission::class.java)
            SubFormat.JSON ->
                jsonSerializer.deserialize(submission, Submission::class.java)
            SubFormat.TSV ->
                tsvDeserializer.deserialize(submission)
        }
    }
}

enum class SubFormat {
    XML, JSON, TSV
}