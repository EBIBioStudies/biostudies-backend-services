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
import com.fasterxml.jackson.module.kotlin.convertValue
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields

class SubmissionJsonDeserializer : StdDeserializer<Submission>(Submission::class.java) {

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Submission {
        val mapper = jp.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jp)

        return Submission(
            accNo = node.findNode<TextNode>(SubFields.ACC_NO.value)?.textValue().orEmpty(),
            attributes = mapper.convertList(node.findNode<JsonNode>(SubFields.ATTRIBUTES.value)),
            section = mapper.convertValue(node.getNode(SubFields.SECTION.value)))
    }
}
