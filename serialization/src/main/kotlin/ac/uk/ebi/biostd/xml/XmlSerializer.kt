package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.common.EitherSerializer
import ac.uk.ebi.biostd.xml.desirializer.AttributeXmlDeserializer
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
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table

class XmlSerializer {

    fun serialize(t: Submission): String {
        return xmlMapper.writeValueAsString(t)
    }

    fun deserialize(value: String): Submission {
        return xmlMapper.readValue(value, Submission::class.java)
    }

    companion object {
        val xmlMapper = createMapper()

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

                addDeserializer(Attribute::class.java, AttributeXmlDeserializer())
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
}
