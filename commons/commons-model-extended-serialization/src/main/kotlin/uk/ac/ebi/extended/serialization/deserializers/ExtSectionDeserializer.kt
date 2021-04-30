package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.convertValue
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACC_NO
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_LIST
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TYPE
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.findNode

class ExtSectionDeserializer : JsonDeserializer<ExtSection>() {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): ExtSection {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return ExtSection(
            accNo = node.findNode<TextNode>(ACC_NO)?.textValue(),
            type = node.findNode<TextNode>(TYPE)?.textValue().orEmpty(),
            fileList = node.findNode<JsonNode>(FILE_LIST)?.let { mapper.convertValue<ExtFileList>(it) },
            attributes = mapper.convertList(node.findNode(ExtSerializationFields.ATTRIBUTES)),
            sections = mapper.convertList(node.findNode(ExtSerializationFields.SECTIONS)),
            files = mapper.convertList(node.findNode(ExtSerializationFields.FILES)),
            links = mapper.convertList(node.findNode(ExtSerializationFields.LINKS))
        )
    }
}
