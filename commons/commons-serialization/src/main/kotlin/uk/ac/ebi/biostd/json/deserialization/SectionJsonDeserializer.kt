package uk.ac.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.constants.SectionFields.ACC_NO
import uk.ac.ebi.biostd.model.constants.SectionFields.ATTRIBUTES
import uk.ac.ebi.biostd.model.constants.SectionFields.FILES
import uk.ac.ebi.biostd.model.constants.SectionFields.LINKS
import uk.ac.ebi.biostd.model.constants.SectionFields.SUBSECTIONS
import uk.ac.ebi.biostd.model.constants.SectionFields.TYPE
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.findNode

internal class SectionJsonDeserializer : StdDeserializer<Section>(Section::class.java) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Section {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return Section(
            accNo = node.findNode<TextNode>(ACC_NO.value)?.textValue(),
            type = node.findNode<TextNode>(TYPE.value)?.textValue().orEmpty(),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES.value) { emptyList() },
            links = mapper.convertOrDefault(node, LINKS.value) { mutableListOf() },
            files = mapper.convertOrDefault(node, FILES.value) { mutableListOf() },
            sections = mapper.convertOrDefault(node, SUBSECTIONS.value) { mutableListOf() },
        )
    }
}
