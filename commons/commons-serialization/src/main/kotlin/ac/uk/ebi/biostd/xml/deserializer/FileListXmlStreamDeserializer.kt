package ac.uk.ebi.biostd.xml.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
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

        while(reader.hasNext())
        {
            referencedFiles.add(mapper.readValue(reader, PageTabFile::class.java))
        }

        reader.close()

        return FileList(file.name, referencedFiles.toList())
    }
}

internal class FileXmlStreamDeserializer : StdDeserializer<PageTabFile>(PageTabFile::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PageTabFile {
        var path = ""
        val attributes: MutableList<Attribute> = mutableListOf()
        lateinit var attr:Attribute

        while (p!!.nextToken() != JsonToken.END_OBJECT) {
            val field = p.currentName

            p.nextToken()

            when(field) {
                "path" -> path = p.text.trim()
                "attribute" -> {
//                    p.nextToken()
//                    while(p.nextToken() != JsonToken.END_ARRAY) {
//                        attributes.add(p.readValueAs(Attribute::class.java))
//                    }
                    //p.nextToken()
                    attr = p.readValueAs(Attribute::class.java)
                    attributes.add(attr)
                }
            }
        }

        return PageTabFile(path, attributes = attributes.toList())
    }
}

internal class AttributeXmlStreamDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Attribute {
        var name = ""
        var value = ""

        while (p!!.nextToken() != JsonToken.END_OBJECT) {
            val field = p.currentName

            p.nextToken()

            when(field) {
                "name" -> name = p.text.trim()
                "value" -> value = p.text.trim()
            }
        }

        return Attribute(name, value)
    }
}
