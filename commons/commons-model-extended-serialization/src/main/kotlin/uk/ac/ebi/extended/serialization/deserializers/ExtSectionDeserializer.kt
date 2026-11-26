package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.convertValue
import uk.ac.ebi.biostd.extended.model.ExtFileList
import uk.ac.ebi.biostd.extended.model.ExtLinkList
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.findNode
import uk.ac.ebi.biostd.serialization.extensions.getNode
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACC_NO
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_LIST
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINK_LIST
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTIONS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TYPE

class ExtSectionDeserializer : JsonDeserializer<ExtSection>() {
    override fun deserialize(
        jsonParser: JsonParser,
        ctxt: DeserializationContext,
    ): ExtSection {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)
        return ExtSection(
            accNo = node.findNode<TextNode>(ACC_NO)?.textValue(),
            type = node.getNode<TextNode>(TYPE).textValue(),
            fileList = node.findNode<JsonNode>(FILE_LIST)?.let { mapper.convertValue<ExtFileList>(it) },
            linkList = node.findNode<JsonNode>(LINK_LIST)?.let { mapper.convertValue<ExtLinkList>(it) },
            attributes = mapper.convertOrDefault(node, ATTRIBUTES) { emptyList() },
            sections = mapper.convertOrDefault(node, SECTIONS) { emptyList() },
            files = mapper.convertOrDefault(node, FILES) { emptyList() },
            links = mapper.convertOrDefault(node, LINKS) { emptyList() },
        )
    }
}
