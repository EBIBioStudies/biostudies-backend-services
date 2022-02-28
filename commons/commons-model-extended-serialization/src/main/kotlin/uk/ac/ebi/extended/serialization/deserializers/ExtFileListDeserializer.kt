package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES_URL
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode

internal object FilesType : TypeReference<List<ExtFile>>()

class ExtFileListDeserializer : JsonDeserializer<ExtFileList>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtFileList {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)

        return ExtFileList(
            filePath = node.getNode<TextNode>(FILE_NAME).textValue(),
            filesUrl = node.findNode<TextNode>(FILES_URL)?.textValue(),
            files = mapper.convertOrDefault(node, FILES) { emptyList() }
        )
    }
}
