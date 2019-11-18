package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.JsonFormat
import ac.uk.ebi.biostd.integration.PlainJson
import ac.uk.ebi.biostd.integration.PrettyJson
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.TsvFormat
import ac.uk.ebi.biostd.integration.XmlFormat
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
        XmlFormat -> xmlSerializer.serialize(element)
        PlainJson -> jsonSerializer.serialize(element)
        PrettyJson -> jsonSerializer.serialize(element, true)
        is TsvFormat -> tsvSerializer.serialize(element)
    }

    fun deserializeSubmission(submission: String, format: SubFormat) = when (format) {
        XmlFormat -> xmlSerializer.deserialize(submission)
        is JsonFormat -> jsonSerializer.deserialize(submission)
        is TsvFormat -> tsvSerializer.deserialize(submission)
    }

    inline fun <reified T> deserializeElement(element: String, format: SubFormat) =
        deserializeElement(element, format, T::class.java)

    fun <T> deserializeElement(element: String, format: SubFormat, type: Class<out T>): T = when (format) {
        XmlFormat -> xmlSerializer.deserialize(element, type)
        is JsonFormat -> jsonSerializer.deserialize(element, type)
        is TsvFormat -> tsvSerializer.deserializeElement(element, type)
    }
}
