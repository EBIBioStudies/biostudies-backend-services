package ac.uk.ebi.biostd.xml

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.model.BioFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
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
    fun deserializeFileList(inputStream: InputStream): Flow<BioFile> {
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(inputStream)

        reader.requireEvent(START_DOCUMENT) { "expecting xml document start" }
        reader.requireEvent(START_ELEMENT, "table") { "expected <table>" }

        return flow {
            while (reader.hasNext() && reader.isIgnorable()) reader.nextInIoThread()

            while (reader.eventType == START_ELEMENT && reader.localName == "file") {
                emit(XmlSerializer.mapper.readInIoThread(reader))
                while (reader.hasNext() && reader.isIgnorable()) reader.nextInIoThread()
            }

            reader.requireEvent(END_ELEMENT, "table") { "expected </table>" }
            reader.requireEvent(END_DOCUMENT) { "expecting xml document end" }
        }
    }

    private suspend fun XmlMapper.readInIoThread(reader: XMLStreamReader): BioFile {
        return withContext(Dispatchers.IO) { readStreamValue(reader) }
    }

    private suspend fun XMLStreamReader.nextInIoThread() {
        withContext(Dispatchers.IO) { next() }
    }

    suspend fun serializeFileList(fileList: Flow<BioFile>, outputStream: OutputStream) = withContext(Dispatchers.IO) {
        val streamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream)
        streamWriter.writeStartDocument()
        streamWriter.writeStartElement("table")
        fileList.collect { XmlSerializer.mapper.writeValue(streamWriter, it) }
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
