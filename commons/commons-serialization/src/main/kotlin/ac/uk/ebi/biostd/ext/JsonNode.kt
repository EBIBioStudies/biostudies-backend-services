package ac.uk.ebi.biostd.ext

import com.fasterxml.jackson.databind.JsonNode

internal inline fun <reified T : JsonNode?> JsonNode.findNode(property: String) =
    get(property)?.also {
        require(it is T) {
            "Expecting node: '$this', property: '$property' to be of type '${T::class.java.simpleName}' " +
                "but '${it::class.java.simpleName}' find instead"
        }
    }

internal inline fun <reified T : JsonNode> JsonNode.getNode(property: String) =
    get(property).also {
        checkNotNull(it) { "Expecting to find property with '$property' in node '$this'" }
        require(it is T) {
            "Expecting node: '$this', property: '$property' to be of type '${T::class.java.simpleName}' " +
                "but '${it::class.java.simpleName}' find instead"
        }
    } as T
