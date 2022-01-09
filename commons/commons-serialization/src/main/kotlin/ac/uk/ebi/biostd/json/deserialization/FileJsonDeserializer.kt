package ac.uk.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields.ATTRIBUTES
import ebi.ac.uk.model.constants.FileFields.PATH
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.getNode

internal class FileJsonDeserializer : StdDeserializer<File>(File::class.java) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): File {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return File(
            path = node.getNode<TextNode>(PATH.value).textValue(),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES.value) { emptyList() }
        )
    }
}
