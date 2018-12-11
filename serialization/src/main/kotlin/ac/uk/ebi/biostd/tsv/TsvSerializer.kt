package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.tsv.deserialization.TsvDeserializer
import ac.uk.ebi.biostd.tsv.serialization.TsvToStringSerializer
import ebi.ac.uk.base.splitIgnoringEmpty
import ebi.ac.uk.model.Submission

val WITH_DELIMITER = "(?=%1\$s)"

class TsvSerializer(
    private val tsvSerializer: TsvToStringSerializer = TsvToStringSerializer(),
    private val tsvDeserializer: TsvDeserializer = TsvDeserializer()
) {

    fun serialize(submission: Submission) = tsvSerializer.serialize(submission)

    fun deserialize(submission: String) = tsvDeserializer.deserialize(submission)

    fun deserializeList(submissions: String) =
        submissions.splitIgnoringEmpty(WITH_DELIMITER.format("Submission\t").toRegex()).map(this::deserialize)
}