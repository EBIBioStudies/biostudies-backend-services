package ac.uk.ebi.biostd.xml.deserializer.stream

import ac.uk.ebi.biostd.ext.forEach
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File as PageTabFile
import ebi.ac.uk.model.FileList
import java.io.File
import javax.xml.stream.XMLInputFactory

internal class FileListXmlStreamDeserializer(
    private val xmlMapper: XmlMapper = createXmlMapper()
) {
    companion object {
        fun createXmlMapper(): XmlMapper {
            val module = JacksonXmlModule().apply {
                setDefaultUseWrapper(false)
                addDeserializer(PageTabFile::class.java, FileXmlStreamDeserializer())
                addDeserializer(Attribute::class.java, AttributeXmlStreamDeserializer())
            }

            return XmlMapper(module)
        }
    }

    fun deserialize(file: File): FileList {
        val referencedFiles: MutableList<PageTabFile> = mutableListOf()
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(file.inputStream())

        reader.next()
        reader.forEach { referencedFiles.add(xmlMapper.readValue(reader, PageTabFile::class.java)) }
        reader.close()

        return FileList(file.name, referencedFiles.toList())
    }
}
