package ac.uk.ebi.biostd

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.mapping.FromEntityMapper
import ac.uk.ebi.biostd.tsv.TsvSerializer
import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.ISubmission

class SerializationService(
        private val jsonSerializer: JsonSerializer = JsonSerializer(),
        private val xmlSerializer: XmlSerializer = XmlSerializer(),
        private val tsvSerializer: TsvSerializer = TsvSerializer(),
        private val entityMapper: FromEntityMapper = FromEntityMapper()) {

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

    private fun asSubmission(submission: ISubmission) = entityMapper.toSubmission(submission)

}

enum class SubFormat {
    XML, JSON, TSV
}