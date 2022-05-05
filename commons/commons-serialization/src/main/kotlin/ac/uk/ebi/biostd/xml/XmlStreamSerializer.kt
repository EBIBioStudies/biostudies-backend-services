package ac.uk.ebi.biostd.xml

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.model.BioFile
import java.io.InputStream
import java.io.OutputStream
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamConstants.CHARACTERS
import javax.xml.stream.XMLStreamConstants.COMMENT
import javax.xml.stream.XMLStreamConstants.END_DOCUMENT
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.SPACE
import javax.xml.stream.XMLStreamConstants.START_DOCUMENT
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import javax.xml.stream.XMLStreamReader

class XmlStreamSerializer {
    fun deserializeFileList(inputStream: InputStream): Sequence<BioFile> {
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(inputStream)

        reader.requireEvent(START_DOCUMENT) { "expecting xml document start" }
        reader.requireEvent(START_ELEMENT, "table") { "expected <table>" }
        while (reader.hasNext() && reader.isIgnorable()) reader.next()

        return sequence {
            while (reader.eventType == START_ELEMENT && reader.localName == "file") {
                yield(XmlSerializer.mapper.readStreamValue(reader))
                while (reader.hasNext() && reader.isIgnorable()) reader.next()
            }

            reader.requireEvent(END_ELEMENT, "table") { "expected </table>" }
            reader.requireEvent(END_DOCUMENT) { "expecting xml document end" }
        }
    }

    fun serializeFileList(fileList: Sequence<BioFile>, outputStream: OutputStream) {
        val streamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream)

        streamWriter.writeStartDocument()
        streamWriter.writeStartElement("table")
        fileList.forEach { XmlSerializer.mapper.writeValue(streamWriter, it) }
        streamWriter.writeEndElement()
        streamWriter.writeEndDocument()
    }

    private fun XMLStreamReader.isIgnorable() = eventType in setOf(CHARACTERS, SPACE, COMMENT)

    private fun XMLStreamReader.requireEvent(type: Int, message: () -> String) {
        while (hasNext() && isIgnorable()) next()
        require(eventType == type, message)
        if (hasNext()) next()
    }

    private fun XMLStreamReader.requireEvent(type: Int, name: String, message: () -> String) {
        while (hasNext() && isIgnorable()) next()
        require(eventType == type && localName == name, message)
        if (hasNext()) next()
    }

    private inline fun <reified T : Any> XmlMapper.readStreamValue(reader: XMLStreamReader): T {
        val result = readValue(reader, T::class.java)
        reader.next()
        return result
    }
}
