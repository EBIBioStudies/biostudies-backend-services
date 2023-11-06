package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.NumericNode
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FILEPATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_ID
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_PUBLISHED
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FULL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_MD5
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_REL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_SIZE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType
import uk.ac.ebi.extended.serialization.exception.InvalidExtTypeException
import uk.ac.ebi.serialization.extensions.convertOrDefault
import uk.ac.ebi.serialization.extensions.getNode
import java.nio.file.Paths

class ExtFileDeserializer : JsonDeserializer<ExtFile>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtFile {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)

        return when (val type = ExtType.valueOf(node.getNode<TextNode>(EXT_TYPE).textValue())) {
            is ExtType.NfsFile -> nfsFile(node, mapper)
            is ExtType.FireFile -> fireFile(node, mapper)
            else -> throw InvalidExtTypeException(type.type)
        }
    }

    private fun fireFile(node: JsonNode, mapper: ObjectMapper): FireFile =
        FireFile(
            fireId = node.getNode<TextNode>(FILE_FIRE_ID).textValue(),
            firePath = node.getNode<TextNode>(FILE_FIRE_PATH).textValue(),
            published = node.getNode<BooleanNode>(FILE_FIRE_PUBLISHED).booleanValue(),
            filePath = node.getNode<TextNode>(FILE_FILEPATH).textValue(),
            relPath = node.getNode<TextNode>(FILE_REL_PATH).textValue(),
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<NumericNode>(FILE_SIZE).longValue(),
            type = ExtFileType.fromString(node.getNode<TextNode>(FILE_TYPE).textValue()),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES) { emptyList() }
        )

    private fun nfsFile(node: JsonNode, mapper: ObjectMapper): NfsFile {
        val fullPath = node.getNode<TextNode>(FILE_FULL_PATH).textValue()
        return NfsFile(
            filePath = node.getNode<TextNode>(FILE_FILEPATH).textValue(),
            relPath = node.getNode<TextNode>(FILE_REL_PATH).textValue(),
            fullPath = fullPath,
            file = Paths.get(fullPath).toFile(),
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<NumericNode>(FILE_SIZE).longValue(),
            type = ExtFileType.fromString(node.getNode<TextNode>(FILE_TYPE).textValue()),
            attributes = mapper.convertOrDefault(node, ATTRIBUTES) { emptyList() }
        )
    }
}
