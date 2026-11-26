package uk.ac.ebi.biostd.json.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.TextNode
import uk.ac.ebi.biostd.common.validatedFilePath
import uk.ac.ebi.biostd.model.BioFile
import uk.ac.ebi.biostd.model.constants.FileFields.ATTRIBUTES
import uk.ac.ebi.biostd.model.constants.FileFields.FILE_TYPE
import uk.ac.ebi.biostd.model.constants.FileFields.PATH
import uk.ac.ebi.biostd.model.constants.FileFields.TYPE
import uk.ac.ebi.biostd.serialization.extensions.convertOrDefault
import uk.ac.ebi.biostd.serialization.extensions.findNode
import uk.ac.ebi.biostd.serialization.extensions.getNode

internal class FileJsonDeserializer : StdDeserializer<BioFile>(BioFile::class.java) {
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): BioFile {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)
        val path = node.getNode<TextNode>(PATH.value).textValue()

        return BioFile(
            path = validatedFilePath(path),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES.value) { emptyList() },
            type = node.findNode<TextNode>(TYPE.value)?.textValue() ?: FILE_TYPE.value,
        )
    }
}
