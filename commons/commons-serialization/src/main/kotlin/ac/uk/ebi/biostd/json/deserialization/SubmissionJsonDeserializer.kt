package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_RELEASE_DATE
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.base.like
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.ACC_NO
import ebi.ac.uk.model.constants.SubFields.ATTRIBUTES
import ebi.ac.uk.model.constants.SubFields.RELEASE_DATE
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
        val attributes = mapper.convertOrDefault<List<Attribute>>(node, ATTRIBUTES.value) { emptyList() }
        val releaseDate = attributes.find { it.name like RELEASE_DATE }?.value.orEmpty()
        require(releaseDate.isNotBlank()) { throw InvalidElementException(REQUIRED_RELEASE_DATE) }

        return Submission(
            accNo = node.findNode<TextNode>(ACC_NO.value)?.textValue().orEmpty(),
            attributes = attributes,
            section = mapper.convertOrDefault(node, SECTION.value) { Section() },
        )
    }
}
