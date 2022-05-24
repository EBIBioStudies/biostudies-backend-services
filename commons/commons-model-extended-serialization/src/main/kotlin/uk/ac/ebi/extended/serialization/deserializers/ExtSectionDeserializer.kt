package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACC_NO
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES_URL
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTIONS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TYPE
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode
import java.io.File

class ExtSectionDeserializer : JsonDeserializer<ExtSection>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtSection {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)
        return ExtSection(
            accNo = node.findNode<TextNode>(ACC_NO)?.textValue(),
            type = node.getNode<TextNode>(TYPE).textValue(),
            fileList = node.findNode<JsonNode>(ExtSerializationFields.FILE_LIST)?.let { deserialize(it) },
            attributes = mapper.convertOrDefault(node, ATTRIBUTES) { emptyList() },
            sections = mapper.convertOrDefault(node, SECTIONS) { emptyList() },
            files = mapper.convertOrDefault(node, FILES) { emptyList() },
            links = mapper.convertOrDefault(node, LINKS) { emptyList() },
        )
    }

    private fun deserialize(node: JsonNode): ExtFileList = ExtFileList(
        file = File(node.getNode<TextNode>(FILE).textValue()),
        filePath = node.getNode<TextNode>(FILE_NAME).textValue(),
        filesUrl = node.getNode<TextNode>(FILES_URL).textValue(),
    )
}
