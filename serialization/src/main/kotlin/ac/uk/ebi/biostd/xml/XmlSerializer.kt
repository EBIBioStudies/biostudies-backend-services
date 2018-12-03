package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.common.EitherSerializer
import ac.uk.ebi.biostd.xml.desirializer.AttributeXmlDeserializer
import ac.uk.ebi.biostd.xml.desirializer.DetailsXmlDeserializer
import ac.uk.ebi.biostd.xml.desirializer.FileXmlDeserializer
import ac.uk.ebi.biostd.xml.desirializer.LinkXmlDeserializer
import ac.uk.ebi.biostd.xml.desirializer.SectionXmlDeserializer
import ac.uk.ebi.biostd.xml.desirializer.SubmissionXmlDeserializer
import ac.uk.ebi.biostd.xml.serializer.AttributeDetailsSerializer
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
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class XmlSerializer(
    val mapper: XmlMapper = createMapper(),
    val deserializer: SubmissionXmlDeserializer = createSubDeserializer()
) {

    fun serialize(t: Submission): String {
        return mapper.writeValueAsString(t)
    }

    fun deserialize(value: String): Submission {
        return deserializer.deserialize(
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(InputSource(StringReader(value))).documentElement)
    }

    companion object {

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
                addSerializer(AttributeDetail::class.java, AttributeDetailsSerializer())
            }

            return XmlMapper(module).apply {
                setDefaultUseWrapper(false)
                setSerializationInclusion(NON_NULL)
                setSerializationInclusion(NON_EMPTY)
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            }
        }

        private fun createSubDeserializer(): SubmissionXmlDeserializer {
            val attributeDeserializer = AttributeXmlDeserializer(DetailsXmlDeserializer())
            val sectionXmlDeserializer = SectionXmlDeserializer(
                attributeDeserializer,
                LinkXmlDeserializer(attributeDeserializer),
                FileXmlDeserializer(attributeDeserializer))
            return SubmissionXmlDeserializer(attributeDeserializer, sectionXmlDeserializer)
        }
    }
}
