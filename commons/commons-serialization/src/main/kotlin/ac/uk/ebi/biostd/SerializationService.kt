package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.SubFormat.JSON
import ac.uk.ebi.biostd.SubFormat.JSON_PRETTY
import ac.uk.ebi.biostd.SubFormat.TSV
import ac.uk.ebi.biostd.SubFormat.XML
import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Submission

class SerializationService(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val tsvSerializer: TsvSerializer = TsvSerializer()
) {
    fun <T> serializeElement(element: T, format: SubFormat) = when (format) {
        XML -> xmlSerializer.serialize(element)
        JSON -> jsonSerializer.serialize(element)
        JSON_PRETTY -> jsonSerializer.serialize(element, true)
        TSV -> tsvSerializer.serialize(element)
    }

    fun serializeSubmission(submission: Submission, format: SubFormat) =        serializeElement(submission, format)

    fun deserializeSubmission(submission: String, format: SubFormat) = when (format) {
        XML -> xmlSerializer.deserialize(submission)
        JSON -> jsonSerializer.deserialize(submission)
        JSON_PRETTY -> jsonSerializer.deserialize(submission)
        TSV -> tsvSerializer.deserialize(submission)
    }
}

enum class SubFormat {
    XML, JSON, TSV, JSON_PRETTY
}
