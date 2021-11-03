package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.constants.SUB_SEPARATOR

class TsvSerializer(
    private val tsvSerializer: TsvToStringSerializer = TsvToStringSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer()
) {
    fun <T> serialize(element: T) = tsvSerializer.serialize(element)

    fun <T> deserializeElement(pageTab: String, type: Class<out T>) = tsvDeserializer.deserializeElement(pageTab, type)

    fun deserialize(submission: String) = tsvDeserializer.deserialize(submission)

    fun deserializeList(submissions: String) = submissions.splitIgnoringEmpty(SUB_SEPARATOR).map(::deserialize)
}
