package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FILEPATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_ID
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FULL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_MD5
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_REL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_SIZE
import uk.ac.ebi.extended.serialization.constants.ExtType
import uk.ac.ebi.extended.serialization.exception.InvalidExtTypeException
import uk.ac.ebi.serialization.extensions.convertList
import uk.ac.ebi.serialization.extensions.findNode
import uk.ac.ebi.serialization.extensions.getNode
import java.io.FileNotFoundException
import java.nio.file.Paths

class ExtFileDeserializer : JsonDeserializer<ExtFile>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtFile {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)

        return when (val type = ExtType.valueOf(node.getNode<TextNode>(EXT_TYPE).textValue())) {
            is ExtType.NfsFile -> nfsFile(node, mapper)
            is ExtType.FireFile -> fireFile(node, mapper)
            is ExtType.FireDirectory -> fireDirectory(node, mapper)
            else -> throw InvalidExtTypeException(type.type)
        }
    }

    private fun fireDirectory(node: JsonNode, mapper: ObjectMapper): FireDirectory {
        return FireDirectory(
            filePath = node.getNode<TextNode>(FILE_FILEPATH).textValue(),
            relPath = node.getNode<TextNode>(FILE_REL_PATH).textValue(),
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<IntNode>(FILE_SIZE).longValue(),
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }

    private fun fireFile(node: JsonNode, mapper: ObjectMapper): FireFile {
        return FireFile(
            filePath = node.getNode<TextNode>(FILE_FILEPATH).textValue(),
            relPath = node.getNode<TextNode>(FILE_REL_PATH).textValue(),
            fireId = node.getNode<TextNode>(FILE_FIRE_ID).textValue(),
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<IntNode>(FILE_SIZE).longValue(),
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }

    private fun nfsFile(node: JsonNode, mapper: ObjectMapper): NfsFile {
        val fullPath = node.getNode<TextNode>(FILE_FULL_PATH).textValue()
        val file = Paths.get(fullPath).toFile()
        require(file.exists()) { throw FileNotFoundException(fullPath) }

        return NfsFile(
            filePath = node.getNode<TextNode>(FILE_FILEPATH).textValue(),
            relPath = node.getNode<TextNode>(FILE_REL_PATH).textValue(),
            fullPath = fullPath,
            file = file,
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<IntNode>(FILE_SIZE).longValue(),
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }
}
