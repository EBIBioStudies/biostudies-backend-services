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
    fun <T> serializeElement(element: T, format: SubFormat) = when(format) {
        SubFormat.XML -> xmlSerializer.serialize(element)
        SubFormat.JSON -> jsonSerializer.serialize(element)
        SubFormat.TSV -> tsvSerializer.serialize(element)
    }

    fun serializeSubmission(submission: Submission, format: SubFormat) = serializeElement(submission, format)

    fun deserializeSubmission(submission: String, format: SubFormat) = when (format) {
        SubFormat.XML -> xmlSerializer.deserialize(submission)
        SubFormat.JSON -> jsonSerializer.deserialize(submission)
        SubFormat.TSV -> tsvSerializer.deserialize(submission)
    }
}

enum class SubFormat {
    XML, JSON, TSV
}
