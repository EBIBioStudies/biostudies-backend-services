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
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FIRE_ID
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_MD5
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_PATH
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
            fileName = node.getNode<TextNode>(FILE_NAME).textValue(),
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<IntNode>(FILE_SIZE).longValue(),
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }

    private fun fireFile(node: JsonNode, mapper: ObjectMapper): FireFile {
        return FireFile(
            fileName = node.getNode<TextNode>(FILE_NAME).textValue(),
            fireId = node.getNode<TextNode>(FILE_FIRE_ID).textValue(),
            md5 = node.getNode<TextNode>(FILE_MD5).textValue(),
            size = node.getNode<IntNode>(FILE_SIZE).longValue(),
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }

    private fun nfsFile(node: JsonNode, mapper: ObjectMapper): NfsFile {
        val filePath = node.getNode<TextNode>(FILE).textValue()
        val file = Paths.get(filePath).toFile()
        require(file.exists()) { throw FileNotFoundException(filePath) }

        return NfsFile(
            file = file,
            fileName = node.getNode<TextNode>(FILE_PATH).textValue(),
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }
}
