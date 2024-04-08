package ac.uk.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.ACC_NO
import ebi.ac.uk.model.constants.SubFields.ATTRIBUTES
import ebi.ac.uk.model.constants.SubFields.SECTION
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.findNode

internal class SubmissionJsonDeserializer : StdDeserializer<Submission>(Submission::class.java) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Submission {
        val mapper = jp.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jp)

        return Submission(
            accNo = node.findNode<TextNode>(ACC_NO.value)?.textValue().orEmpty(),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES.value) { emptyList() },
            section = mapper.convertOrDefault(node, SECTION.value) { Section() },
        )
    }
}
