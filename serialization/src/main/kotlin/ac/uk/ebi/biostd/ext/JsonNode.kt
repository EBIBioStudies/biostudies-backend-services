package ac.uk.ebi.biostd.ext

import com.fasterxml.jackson.databind.JsonNode

fun JsonNode.getRequired(field: String): String {
    return get(field)?.asText() ?: error("Expecting to find property with '$field' in node '$this'")
}
