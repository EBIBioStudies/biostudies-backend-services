package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.common.validatedFilePath
import ac.uk.ebi.biostd.xml.deserializer.AttributeXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.DetailsXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.FileStandaloneXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.LinkStandaloneXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.SectionStandaloneXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.SubmissionStandaloneXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.exception.InvalidXmlPageTabElementException
import ac.uk.ebi.biostd.xml.deserializer.exception.UnexpectedXmlPageTabElementException
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
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.AttributeFields
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.LinkFields
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.util.collections.ifRight
import org.xml.sax.InputSource
import uk.ac.ebi.serialization.serializers.EitherSerializer
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

internal class XmlSerializer {
    fun <T> serialize(element: T): String = mapper.writeValueAsString(element)

    fun deserialize(value: String): Submission = deserialize(value, Submission::class.java)

    fun <T> deserialize(element: String, type: Class<out T>): T {
        var deserialized: Any? = null
        val xml = buildXmlFile(element)
        val attributeDeserializer = AttributeXmlDeserializer(DetailsXmlDeserializer())
        val filesDeserializer = FileStandaloneXmlDeserializer(attributeDeserializer)
        val linksDeserializer = LinkStandaloneXmlDeserializer(attributeDeserializer)
        val sectionDeserializer =
            SectionStandaloneXmlDeserializer(attributeDeserializer, linksDeserializer, filesDeserializer)
        val submissionDeserializer = SubmissionStandaloneXmlDeserializer(attributeDeserializer, sectionDeserializer)

        when (type) {
            BioFile::class.java -> deserialized = mapper.readValue(element, BioFile::class.java)
            Link::class.java -> deserialized = mapper.readValue(element, Link::class.java)
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
                addSerializer(BioFile::class.java, FileSerializer())
                addSerializer(Table::class.java, TableSerializer())

                addDeserializer(Submission::class.java, SubmissionXmlDeserializer())
                addDeserializer(Section::class.java, SectionXmlDeserializer())
                addDeserializer(Attribute::class.java, AttributeXmlStreamDeserializer())
                addDeserializer(Link::class.java, LinkXmlDeserializer())
                addDeserializer(BioFile::class.java, FileXmlStreamDeserializer())
            }

            return XmlMapper(module).apply {
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

class FileXmlStreamDeserializer : StdDeserializer<BioFile>(BioFile::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BioFile {
        val mapper = p.codec as XmlMapper
        val node = p.readValueAsTree<TreeNode>()
        val path = (node.get(FileFields.PATH.value) as TextNode).textValue().trim()

        return BioFile(
            path = validatedFilePath(path),
            attributes = mapper.convertArray(node, "attributes", "attribute", Array<Attribute>::class.java).toList()
        )
    }
}

class AttributeXmlStreamDeserializer : StdDeserializer<Attribute>(Attribute::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Attribute {
        val node = p.readValueAsTree<TreeNode>()

        return Attribute(
            name = (node.get(AttributeFields.NAME.value) as TextNode).textValue().trim(),
            value = (node.get(AttributeFields.VALUE.value) as TextNode).textValue().trim()
        )
    }
}

class SectionXmlDeserializer : StdDeserializer<Section>(Section::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Section {
        val mapper = p.codec as XmlMapper
        val node = p.readValueAsTree<TreeNode>()

        return Section(
            accNo = (node.get(SectionFields.ACC_NO.value) as TextNode).textValue().trim(),
            type = (node.get(SectionFields.TYPE.value) as TextNode).textValue().trim(),
            attributes = mapper.convertArray(node, "attributes", "attribute", Array<Attribute>::class.java).toList(),
            links = mutableListOf(),
            files = mutableListOf(),
            sections = mutableListOf()
        )
    }
}

class SubmissionXmlDeserializer : StdDeserializer<Submission>(Submission::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Submission {
        val mapper = p.codec as XmlMapper
        val node = p.readValueAsTree<TreeNode>()
        val sectionNode = node.get(SubFields.SECTION.value) as ObjectNode

        return Submission(
            accNo = (node.get(SubFields.ACC_NO.value) as TextNode).textValue().trim(),
            attributes = mapper.convertArray(node, "attributes", "attribute", Array<Attribute>::class.java).toList(),
            section = mapper.convertValue(sectionNode, Section::class.java)
        )
    }
}

class LinkXmlDeserializer : StdDeserializer<Link>(Link::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Link {
        val mapper = p.codec as XmlMapper
        val node = p.readValueAsTree<TreeNode>()

        return Link(
            url = (node.get(LinkFields.URL.value) as TextNode).textValue().trim(),
            attributes = mapper.convertArray(node, "attributes", "attribute", Array<Attribute>::class.java).toList()
        )
    }
}

fun <T> XmlMapper.convertArray(
    node: TreeNode,
    arrayName: String,
    elementName: String,
    toValueType: Class<T>
): T = convertValue(node.getArrayAttribute(arrayName, elementName), toValueType)

fun TreeNode.getArrayAttribute(arrayName: String, elementName: String): ArrayNode =
    when (val node = get(arrayName)?.get(elementName)) {
        is ArrayNode -> node
        is ObjectNode -> ArrayNode(JsonNodeFactory.instance, listOf(node))
        null -> ArrayNode(JsonNodeFactory.instance, emptyList())
        else -> TODO()
    }
