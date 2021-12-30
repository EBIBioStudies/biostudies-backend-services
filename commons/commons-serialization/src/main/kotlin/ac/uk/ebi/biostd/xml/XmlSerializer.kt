package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.xml.deserializer.AttributeXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.DetailsXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.FileXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.LinkXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.SectionXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.SubmissionXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.exception.InvalidXmlPageTabElementException
import ac.uk.ebi.biostd.xml.deserializer.exception.UnexpectedXmlEndElementTypeException
import ac.uk.ebi.biostd.xml.deserializer.exception.UnexpectedXmlPageTabElementException
import ac.uk.ebi.biostd.xml.deserializer.exception.UnexpectedXmlStartElementTypeException
import ac.uk.ebi.biostd.xml.serializer.AttributeSerializer
import ac.uk.ebi.biostd.xml.serializer.FileSerializer
import ac.uk.ebi.biostd.xml.serializer.LinkSerializer
import ac.uk.ebi.biostd.xml.serializer.SectionSerializer
import ac.uk.ebi.biostd.xml.serializer.SubmissionSerializer
import ac.uk.ebi.biostd.xml.serializer.TableSerializer
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.AttributeFields
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.util.collections.ifRight
import org.xml.sax.InputSource
import uk.ac.ebi.serialization.serializers.EitherSerializer
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamConstants.CHARACTERS
import javax.xml.stream.XMLStreamConstants.END_ELEMENT
import javax.xml.stream.XMLStreamConstants.START_DOCUMENT
import javax.xml.stream.XMLStreamConstants.START_ELEMENT
import javax.xml.stream.XMLStreamReader

internal class XmlSerializer {
    fun deserializeFileList(file: java.io.File): Sequence<File> {
        val inputStream = file.inputStream()
        val reader = XMLInputFactory.newFactory().createXMLStreamReader(inputStream)

        reader.ignoreCharacters()
        reader.requireStartElement("table")
        reader.ignoreCharacters()
        return sequence {
            while (reader.eventType == START_ELEMENT && reader.localName == "file") {
                yield(mapper.readValue(reader, File::class.java))
                reader.next()
                reader.ignoreCharacters()
            }
            reader.requireEndElement("table")
            inputStream.close()
        }
    }

    private fun XMLStreamReader.ignoreCharacters() {
        while (hasNext() && (eventType == CHARACTERS || eventType == START_DOCUMENT)) next()
    }

    private fun XMLStreamReader.requireStartElement(type: String) {
        if (eventType != START_ELEMENT || localName != type) throw UnexpectedXmlStartElementTypeException(type)
        next()
    }

    private fun XMLStreamReader.requireEndElement(type: String) {
        if (eventType != END_ELEMENT || localName != type) throw UnexpectedXmlEndElementTypeException(type)
        next()
    }

    fun serializeFileList(fileList: Sequence<File>, file: java.io.File) {
        val outputStream = file.outputStream()
        val streamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(outputStream)

        streamWriter.writeStartDocument()
        streamWriter.writeStartElement("table")
        fileList.forEach { mapper.writeValue(streamWriter, it) }
        streamWriter.writeEndElement()
        streamWriter.writeEndDocument()

        outputStream.close()
    }

    fun <T> serialize(element: T): String = mapper.writeValueAsString(element)

    fun deserialize(value: String): Submission = deserialize(value, Submission::class.java)

    fun <T> deserialize(element: String, type: Class<out T>): T {
        var deserialized: Any? = null
        val xml = buildXmlFile(element)
        val attributeDeserializer = AttributeXmlDeserializer(DetailsXmlDeserializer())
        val filesDeserializer = FileXmlDeserializer(attributeDeserializer)
        val linksDeserializer = LinkXmlDeserializer(attributeDeserializer)
        val sectionDeserializer = SectionXmlDeserializer(attributeDeserializer, linksDeserializer, filesDeserializer)
        val submissionDeserializer = SubmissionXmlDeserializer(attributeDeserializer, sectionDeserializer)

        when (type) {
            File::class.java -> deserialized = filesDeserializer.deserialize(xml)
            Link::class.java -> deserialized = linksDeserializer.deserialize(xml)
            Section::class.java -> deserialized = sectionDeserializer.deserialize(xml)
            Submission::class.java -> deserialized = submissionDeserializer.deserialize(xml)
            FilesTable::class.java -> filesDeserializer.deserializeFilesTable(xml).ifRight { deserialized = it }
            LinksTable::class.java -> linksDeserializer.deserializeLinksTable(xml).ifRight { deserialized = it }
            else -> throw InvalidXmlPageTabElementException()
        }

        return type.cast(deserialized) ?: throw UnexpectedXmlPageTabElementException()
    }

    companion object {
        val mapper: XmlMapper = createMapper()

        private fun createMapper(): XmlMapper {
            val module = JacksonXmlModule().apply {
                setDefaultUseWrapper(false)
                addSerializer(Submission::class.java, SubmissionSerializer())
                addSerializer(Section::class.java, SectionSerializer())
                addSerializer(Attribute::class.java, AttributeSerializer())
                addSerializer(Either::class.java, EitherSerializer())
                addSerializer(Link::class.java, LinkSerializer())
                addSerializer(File::class.java, FileSerializer())
                addSerializer(Table::class.java, TableSerializer())

                addDeserializer(File::class.java, FileXmlStreamDeserializer())
                addDeserializer(Attribute::class.java, AttributeXmlStreamDeserializer())
            }

            return XmlMapper(module).apply {
                setDefaultUseWrapper(false)
                setSerializationInclusion(NON_NULL)
                setSerializationInclusion(NON_EMPTY)
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false)
            }
        }
    }

    private fun buildXmlFile(value: String) =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(value))).documentElement
}

class FileXmlStreamDeserializer : StdDeserializer<File>(File::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): File {
        val mapper = p.codec as ObjectMapper
        val node = p.readValueAsTree<TreeNode>()

        return File(
            path = (node.get(FileFields.PATH.value) as TextNode).textValue(),
            size = (node.get(FileFields.SIZE.value) as TextNode).textValue().toLong(),
            attributes = mapper.convertValue(
                node.getArrayAttribute("attributes", "attribute"),
                Array<Attribute>::class.java
            ).toList()
        )
    }
}

class AttributeXmlStreamDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Attribute {
        val node = p.readValueAsTree<TreeNode>()
        return Attribute(
            name = (node.get(AttributeFields.NAME.value) as TextNode).textValue(),
            value = (node.get(AttributeFields.VALUE.value) as TextNode).textValue()
        )
    }
}

fun TreeNode.getArrayAttribute(arrayName: String, elementName: String): ArrayNode {
    val node = get(arrayName).get(elementName)
    return when (node) {
        is ArrayNode -> node
        is ObjectNode -> ArrayNode(JsonNodeFactory.instance, listOf(node))
        else -> TODO()
    }
}
