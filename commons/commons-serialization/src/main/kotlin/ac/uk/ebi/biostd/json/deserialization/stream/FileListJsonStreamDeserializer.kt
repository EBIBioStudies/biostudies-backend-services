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
import java.io.File
import ebi.ac.uk.model.File as RefFile

internal class FileListJsonStreamDeserializer {
    fun deserialize(file: File): FileList {
        val jp = JsonFactory().createParser(file)
        val files = jp.parseArray { parseFile(it) }
        jp.close()
        return FileList(file.name, files)
    }

    private fun parseFile(jp: JsonParser): RefFile {
        require(jp.currentToken == START_OBJECT) { "expected start object token" }
        val fileBuilder = FileBuilder()
        while (jp.nextToken() != END_OBJECT) {
            when (jp.currentName) {
                "path" -> fileBuilder.path = jp.nextTextValue()
                "attributes" -> fileBuilder.attributes = jp.parseArray { parseAttribute(it) }
                else -> IllegalArgumentException("unknown property with name ${jp.currentName}")
            }
        }
        return fileBuilder.build()
    }

    private fun parseAttribute(jp: JsonParser): Attribute {
        require(jp.currentToken == START_OBJECT) { "expected start object token" }
        val attribute = AttributeBuilder()
        while (jp.nextToken() != END_OBJECT) {
            when (jp.currentName) {
                "name" -> attribute.name = jp.nextTextValue()
                "value" -> attribute.value = jp.nextTextValue()
                else -> IllegalArgumentException("unknown property with name ${jp.currentName}")
            }
        }
        return attribute.build()
    }
}
