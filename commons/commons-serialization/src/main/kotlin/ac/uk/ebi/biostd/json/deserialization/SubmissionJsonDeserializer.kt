package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_RELEASE_DATE
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import uk.ac.ebi.biostd.base.like
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.model.constants.SubFields.ACC_NO
import uk.ac.ebi.biostd.model.constants.SubFields.ATTRIBUTES
import uk.ac.ebi.biostd.model.constants.SubFields.RELEASE_DATE
import uk.ac.ebi.biostd.model.constants.SubFields.SECTION
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.findNode

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
