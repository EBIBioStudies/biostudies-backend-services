package ac.uk.ebi.biostd.xml

import uk.ac.ebi.serialization.serializers.EitherSerializer
import ac.uk.ebi.biostd.xml.deserializer.AttributeXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.DetailsXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.FileXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.LinkXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.SectionXmlDeserializer
import ac.uk.ebi.biostd.xml.deserializer.SubmissionXmlDeserializer
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
import com.fasterxml.jackson.databind.SerializationFeature
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
import ebi.ac.uk.util.collections.ifRight
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

internal class XmlSerializer {

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
            }

            return XmlMapper(module).apply {
                setDefaultUseWrapper(false)
                setSerializationInclusion(NON_NULL)
                setSerializationInclusion(NON_EMPTY)
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            }
        }
    }

    private fun buildXmlFile(value: String) =
        DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(InputSource(StringReader(value))).documentElement
}
