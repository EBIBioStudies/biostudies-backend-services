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
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

class FileJsonDeserializer : StdDeserializer<File>(File::class.java) {

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): File {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return File(
            name = node.getNode<TextNode>(FileFields.NAME.value).textValue(),
            attributes = mapper.convertList(node.findNode<JsonNode>(FileFields.ATTRIBUTES.value)))
    }
}
