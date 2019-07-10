package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JSON
import ac.uk.ebi.biostd.integration.SubFormat.JSON_PRETTY
import ac.uk.ebi.biostd.integration.SubFormat.TSV
import ac.uk.ebi.biostd.integration.SubFormat.XML
import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Submission

internal class PagetabSerializer(
    private val jsonSerializer: JsonSerializer = JsonSerializer(),
    private val xmlSerializer: XmlSerializer = XmlSerializer(),
    private val tsvSerializer: TsvSerializer = TsvSerializer()
) {
    fun serializeSubmission(submission: Submission, format: SubFormat): String = serializeElement(submission, format)

    fun <T> serializeElement(element: T, format: SubFormat) = when (format) {
        XML -> xmlSerializer.serialize(element)
        JSON -> jsonSerializer.serialize(element)
        JSON_PRETTY -> jsonSerializer.serialize(element, true)
        TSV -> tsvSerializer.serialize(element)
    }

    fun deserializeSubmission(submission: String, format: SubFormat) = when (format) {
        XML -> xmlSerializer.deserialize(submission)
        JSON -> jsonSerializer.deserialize(submission)
        JSON_PRETTY -> jsonSerializer.deserialize(submission)
        TSV -> tsvSerializer.deserialize(submission)
    }

    inline fun <reified T> deserializeElement(element: String, format: SubFormat) =
        deserializeElement(element, format, T::class.java)

    fun <T> deserializeElement(element: String, format: SubFormat, type: Class<out T>): T = when (format) {
        XML -> xmlSerializer.deserialize(element, type)
        JSON -> jsonSerializer.deserialize(element, type)
        JSON_PRETTY -> jsonSerializer.deserialize(element, type)
        TSV -> tsvSerializer.deserializeElement(element, type)
    }
}
