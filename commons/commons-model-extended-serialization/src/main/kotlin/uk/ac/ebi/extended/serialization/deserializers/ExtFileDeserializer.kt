package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FILEPATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_FULL_PATH
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_REL_PATH
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
            is ExtType.FireFile -> TODO()
            else -> throw InvalidExtTypeException(type.type)
        }
    }

    private fun nfsFile(node: JsonNode, mapper: ObjectMapper): NfsFile {
        val filePath = node.getNode<TextNode>(FILE).textValue()
        val file = Paths.get(filePath).toFile()
        require(file.exists()) { throw FileNotFoundException(filePath) }

        return NfsFile(
            fileName = node.getNode<TextNode>(FILE_NAME).textValue(),
            filePath = node.getNode<TextNode>(FILE_FILEPATH).textValue(),
            relPath = node.getNode<TextNode>(FILE_REL_PATH).textValue(),
            fullPath = node.getNode<TextNode>(FILE_FULL_PATH).textValue(),
            file = file,
            attributes = mapper.convertList(node.findNode(ATTRIBUTES))
        )
    }
}
