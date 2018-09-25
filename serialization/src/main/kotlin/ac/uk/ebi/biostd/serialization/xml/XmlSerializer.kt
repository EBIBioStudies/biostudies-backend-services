package ac.uk.ebi.biostd.serialization.xml

import ac.uk.ebi.biostd.serialization.common.EitherSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.AttributeSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.FileSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.LinkSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.SectionSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.SubmissionSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.TableSerializer
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.Table
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator


class XmlSerializer {

    fun serialize(submission: Any): String {
        return mapper.writeValueAsString(submission)
    }

    companion object {
        val mapper = createMapper()

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
}
