package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.common.NAME
import ac.uk.ebi.biostd.common.VALUE
import ac.uk.ebi.biostd.json.exception.NoAttributeValueException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import uk.ac.ebi.serialization.extensions.getNode

internal object AttributeJsonDeserializerHelper {
    fun deserializeNameValue(node: JsonNode): Pair<String, String> {
        val name = node.getNode<TextNode>(NAME).textValue()
        val value = node.getNode<TextNode>(VALUE).textValue()
        require(value.isNotBlank()) { throw NoAttributeValueException(name) }

        return name to value
    }
}
