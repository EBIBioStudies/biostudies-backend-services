package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_LINK_URL
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.constants.ATTRIBUTES
import uk.ac.ebi.biostd.model.constants.LinkFields
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.getNode

internal class LinkJsonDeserializer : StdDeserializer<Link>(Link::class.java) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Link {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val url = node.getNode<TextNode>(LinkFields.URL.value).textValue()
        require(url.isNotBlank()) { throw InvalidElementException(REQUIRED_LINK_URL) }

        return Link(
            url = url,
            attributes = mapper.convertOrDefault(node, ATTRIBUTES) { emptyList() },
        )
    }
}
