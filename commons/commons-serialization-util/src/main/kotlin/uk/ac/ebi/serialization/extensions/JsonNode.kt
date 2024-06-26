package uk.ac.ebi.serialization.extensions

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode

fun JsonNode.getProperty(name: String): String {
    val properties = name.split(".")
    var node = this
    for (p in properties) {
        node = node.getNode(p)
    }
    return node.textValue()
}

inline fun <reified T : JsonNode> JsonNode.findNode(property: String): T? =
    when (val node = get(property)) {
        is NullNode -> null
        null -> null
        else -> ensureType<T>(node, property)
    }

inline fun <reified T : JsonNode> JsonNode.getNode(property: String): T =
    when (val node = get(property)) {
        is NullNode, null -> error("Expecting to find property with '$property' in node '$this'")
        else -> ensureType(node, property)
    }

inline fun <reified T : JsonNode?> JsonNode.ensureType(
    node: JsonNode,
    property: String,
): T {
    require(node is T) {
        val type = node::class.java.simpleName
        val expectedType = T::class.java.simpleName
        "Expecting node: '$this', property: '$property' to be of type '$expectedType' but '$type' was found instead"
    }

    return node
}
