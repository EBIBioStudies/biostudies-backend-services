package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.ext.processCurrentElement
import ac.uk.ebi.biostd.ext.mapList
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File as PageTabFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.builders.AttributeBuilder
import ebi.ac.uk.model.builders.FileBuilder
import ebi.ac.uk.model.constants.AttributeFields
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.SectionFields
import java.io.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader

internal class FileListXmlStreamDeserializer {
    fun deserialize(file: File): FileList {
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(file.inputStream())
        val referencedFiles = reader.mapList(SectionFields.FILES.value, FileFields.FILE.value) { parseFile(it) }

        reader.close()

        return FileList(file.name, referencedFiles)
    }

    private fun parseFile(reader: XMLStreamReader): PageTabFile {
        val fileBuilder = FileBuilder()
        val attributes: MutableList<Attribute> = mutableListOf()

        reader.processCurrentElement {
            when (reader.localName) {
                FileFields.PATH.value -> fileBuilder.path = reader.elementText.trim()
                AttributeFields.ATTRIBUTE.value -> attributes.add(parseAttribute(reader))
            }
        }

        fileBuilder.attributes = attributes.toList()

        return fileBuilder.build()
    }

    private fun parseAttribute(reader: XMLStreamReader): Attribute {
        val attributeBuilder = AttributeBuilder()

        reader.processCurrentElement {
            when (reader.localName) {
                AttributeFields.NAME.value -> attributeBuilder.name = reader.elementText.trim()
                AttributeFields.VALUE.value -> attributeBuilder.value = reader.elementText.trim()
            }
        }

        return attributeBuilder.build()
    }
}
