package ac.uk.ebi.biostd.xml

import com.fasterxml.jackson.databind.ObjectMapper
import ebi.ac.uk.model.File
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamConstants.CHARACTERS
import javax.xml.stream.XMLStreamConstants.END_DOCUMENT
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.START_DOCUMENT
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import javax.xml.stream.XMLStreamReader

class XmlStreamSerializer {
    fun deserializeFileList(file: java.io.File): Sequence<File> {
        val inputStream = file.inputStream()
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(inputStream)

        reader.requireEvent(START_DOCUMENT) { "expecting xml document start" }
        reader.requireEvent(START_ELEMENT, "table") { "expected <table>" }

        return sequence {
            while (reader.eventType == START_ELEMENT && reader.localName == "file") {
                yield(XmlSerializer.mapper.readStreamValue(reader))
                while (reader.hasNext() && reader.isIgnorable()) reader.next()
            }

            reader.requireEvent(END_ELEMENT, "table") { "expected </table>" }
            reader.requireEvent(END_DOCUMENT) { "expecting xml document end" }
            inputStream.close()
        }
    }

    fun serializeFileList(fileList: Sequence<File>, file: java.io.File) {
        val outputStream = file.outputStream()
        val streamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream)

        streamWriter.writeStartDocument()
        streamWriter.writeStartElement("table")
        fileList.forEach { XmlSerializer.mapper.writeValue(streamWriter, it) }
        streamWriter.writeEndElement()
        streamWriter.writeEndDocument()

        outputStream.close()
    }

    private fun XMLStreamReader.isIgnorable() = eventType == CHARACTERS || eventType == START_DOCUMENT

    private fun XMLStreamReader.requireEvent(type: Int, message: () -> String) {
        while (hasNext() && isIgnorable()) next()
        require(eventType == type, message)
    }

    private fun XMLStreamReader.requireEvent(type: Int, name: String, message: () -> String) {
        while (hasNext() && isIgnorable()) next()
        require(eventType == type && localName == name, message)
    }

    private inline fun <reified T : Any> ObjectMapper.readStreamValue(reader: XMLStreamReader): T {
        val result = XmlSerializer.mapper.readValue(reader, T::class.java)
        reader.next()
        return result
    }
}


