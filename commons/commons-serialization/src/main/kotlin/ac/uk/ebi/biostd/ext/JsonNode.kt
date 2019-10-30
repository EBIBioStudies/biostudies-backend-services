package ac.uk.ebi.biostd.ext

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode

internal inline fun <reified T : JsonNode> JsonNode.findNode(property: String): T? =
    when (val node = get(property)) {
        is NullNode -> null
        null -> null
        else -> ensureType<T>(node, property)
    }

internal inline fun <reified T : JsonNode> JsonNode.getNode(property: String): T =
    when (val node = get(property)) {
        is NullNode, null -> throw IllegalStateException("Expecting to find property with '$property' in node '$this'")
        else -> ensureType(node, property)
    }

private inline fun <reified T : JsonNode?> JsonNode.ensureType(node: JsonNode, property: String): T {
    require(node is T) {
        val type = node::class.java.simpleName
        val expectedType = T::class.java.simpleName
        "Expecting node: '$this', property: '$property' to be of type '$expectedType' but '$type' find instead"
    }

    return node
}
