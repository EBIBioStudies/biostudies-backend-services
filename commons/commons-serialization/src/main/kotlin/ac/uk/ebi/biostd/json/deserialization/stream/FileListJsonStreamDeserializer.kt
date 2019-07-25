package ac.uk.ebi.biostd.json.deserialization.stream

import ac.uk.ebi.biostd.ext.parseArray
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.END_OBJECT
import com.fasterxml.jackson.core.JsonToken.START_OBJECT
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.builders.AttributeBuilder
import ebi.ac.uk.model.builders.FileBuilder
import ebi.ac.uk.model.constants.AttributeFields
import ebi.ac.uk.model.constants.FileFields
import java.io.File
import ebi.ac.uk.model.File as RefFile

internal class FileListJsonStreamDeserializer {
    fun deserialize(file: File): FileList {
        val jsonParser = JsonFactory().createParser(file)
        val files = jsonParser.parseArray { parseFile(it) }

        jsonParser.close()

        return FileList(file.name, files)
    }

    private fun parseFile(jsonParser: JsonParser): RefFile {
        requireStartObject(jsonParser)
        val fileBuilder = FileBuilder()

        while (jsonParser.nextToken() != END_OBJECT) {
            when (jsonParser.currentName) {
                FileFields.PATH.value -> fileBuilder.path = jsonParser.nextTextValue()
                FileFields.ATTRIBUTES.value -> fileBuilder.attributes = jsonParser.parseArray { parseAttribute(it) }
            }
        }

        return fileBuilder.build()
    }

    private fun parseAttribute(jsonParser: JsonParser): Attribute {
        requireStartObject(jsonParser)
        val attribute = AttributeBuilder()

        while (jsonParser.nextToken() != END_OBJECT) {
            when (jsonParser.currentName) {
                AttributeFields.NAME.value -> attribute.name = jsonParser.nextTextValue()
                AttributeFields.VALUE.value -> attribute.value = jsonParser.nextTextValue()
                else -> unknownPropertyError(jsonParser)
            }
        }

        return attribute.build()
    }

    private fun requireStartObject(jsonParser: JsonParser) =
        require(jsonParser.currentToken == START_OBJECT) { "Expected start object token" }

    private fun unknownPropertyError(jsonParser: JsonParser) {
        throw IllegalArgumentException("Unknown property with name ${jsonParser.currentName}")
    }
}
