package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFileList
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode

class ExtFileListDeserializer : JsonDeserializer<ExtFileList>() {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): ExtFileList {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return ExtFileList(
            fileName = node.getNode<TextNode>(ExtSerializationFields.FILE_NAME).textValue(),
            files = mapper.convertList(node.findNode(ExtSerializationFields.FILES))
        )
    }
}
