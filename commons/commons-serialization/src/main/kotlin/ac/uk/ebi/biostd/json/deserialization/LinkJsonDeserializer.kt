package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.ext.convertList
import ac.uk.ebi.biostd.ext.findNode
import ac.uk.ebi.biostd.ext.getNode
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.constants.LinkFields

internal class LinkJsonDeserializer : StdDeserializer<Link>(Link::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Link {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return Link(
            url = node.getNode<TextNode>(LinkFields.URL.value).textValue(),
            attributes = mapper.convertList(node.findNode<JsonNode>(LinkFields.ATTRIBUTES.value)))
    }
}
