package ac.uk.ebi.biostd.xml.deserializer.stream

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File as PageTabFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.builders.AttributeBuilder
import ebi.ac.uk.model.builders.FileBuilder
import ebi.ac.uk.model.constants.AttributeFields.ATTRIBUTE
import ebi.ac.uk.model.constants.AttributeFields.NAME
import ebi.ac.uk.model.constants.AttributeFields.VALUE
import ebi.ac.uk.model.constants.FileFields.ATTRIBUTES
import ebi.ac.uk.model.constants.FileFields.FILE
import ebi.ac.uk.model.constants.FileFields.PATH
import ebi.ac.uk.model.constants.SectionFields.FILES
import java.io.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import javax.xml.stream.XMLStreamReader
import uk.ac.ebi.serialization.extensions.contentAsString
import uk.ac.ebi.serialization.extensions.forEach
import uk.ac.ebi.serialization.extensions.map
import uk.ac.ebi.serialization.extensions.use

// TODO(review because is not used)
internal class FileListXmlStreamDeserializer {
    fun deserialize(file: File): FileList {
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(file.inputStream())

        return FileList(file.name, reader.use(this::parseFiles))
    }

    private fun parseFiles(reader: XMLStreamReader): List<PageTabFile> {
        while (reader.hasNext() && (reader.eventType != START_ELEMENT || reader.localName != FILES.value)) reader.next()

        return reader.map(FILES.value) { parseFile(this) }
    }

    private fun parseFile(reader: XMLStreamReader): PageTabFile {
        val file = FileBuilder()

        reader.forEach(FILE.value) {
            when (localName) {
                PATH.value -> file.path = contentAsString
                ATTRIBUTES.value -> file.attributes = reader.map(ATTRIBUTES.value) { parseAttribute(this) }
            }
        }

        return file.build()
    }

    private fun parseAttribute(reader: XMLStreamReader): Attribute {
        val attributeBuilder = AttributeBuilder()

        reader.forEach(ATTRIBUTE.value) {
            when (localName) {
                NAME.value -> attributeBuilder.name = contentAsString
                VALUE.value -> attributeBuilder.value = contentAsString
            }
        }

        return attributeBuilder.build()
    }
}
