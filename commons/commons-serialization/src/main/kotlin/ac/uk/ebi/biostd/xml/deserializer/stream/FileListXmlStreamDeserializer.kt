package ac.uk.ebi.biostd.xml.deserializer.stream

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File as PageTabFile
import ebi.ac.uk.model.FileList
import java.io.File
import javax.xml.stream.XMLInputFactory

internal class FileListXmlStreamDeserializer {
    fun deserialize(file: File): FileList {
        val referencedFiles: MutableList<PageTabFile> = mutableListOf()
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(file.inputStream())
        val module = JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
            addDeserializer(PageTabFile::class.java, FileXmlStreamDeserializer())
            addDeserializer(Attribute::class.java, AttributeXmlStreamDeserializer())
        }
        val mapper = XmlMapper(module)
        reader.next()

        while (reader.hasNext()) {
            try {
                referencedFiles.add(mapper.readValue(reader, PageTabFile::class.java))
            } catch (exception: NoSuchElementException) {
                break
            }
        }

        reader.close()

        return FileList(file.name, referencedFiles.toList())
    }
}
