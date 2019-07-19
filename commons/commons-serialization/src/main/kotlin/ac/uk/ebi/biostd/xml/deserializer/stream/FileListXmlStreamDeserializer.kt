package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.ext.mapList
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File as PageTabFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.builders.AttributeBuilder
import ebi.ac.uk.model.builders.FileBuilder
import java.io.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader

internal class FileListXmlStreamDeserializer {
    fun deserialize(file: File): FileList {
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(file.inputStream())
        val referencedFiles = reader.mapList("files", "file") { parseFile(it) }

        reader.close()

        return FileList(file.name, referencedFiles)
    }

    private fun parseFile(reader: XMLStreamReader): PageTabFile {
        var end = false
        val fileBuilder = FileBuilder()
        val attributes: MutableList<Attribute> = mutableListOf()

        while(reader.hasNext().and(end.not())) {
            reader.next()
            when(reader.eventType) {
                XMLStreamReader.START_ELEMENT -> {
                    when(reader.localName) {
                        "path" -> fileBuilder.path = reader.elementText.trim()
                        "attribute" -> attributes.add(parseAttribute(reader))
                    }
                }
                XMLStreamReader.END_ELEMENT -> end = true
            }
        }

        fileBuilder.attributes = attributes.toList()

        return fileBuilder.build()
    }

    private fun parseAttribute(reader: XMLStreamReader): Attribute {
        var end = false
        val attributeBuilder = AttributeBuilder()

        while(reader.hasNext().and(end.not())) {
            reader.next()
            when (reader.eventType) {
                XMLStreamReader.START_ELEMENT -> {
                    when (reader.localName) {
                        "name" -> attributeBuilder.name = reader.elementText.trim()
                        "value" -> attributeBuilder.value = reader.elementText.trim()
                    }
                }
                XMLStreamReader.END_ELEMENT -> end = true
            }
        }

        return attributeBuilder.build()
    }
}
