package ac.uk.ebi.biostd.serialization.xml

import ac.uk.ebi.biostd.serialization.xml.serializer.AttrListSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.AttributeSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.SubmissionSerializer
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.Submission
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator


class XmlSerializer {

    fun serialize(submission: Submission): String {
        return mapper.writeValueAsString(submission)
    }

    companion object {
        val mapper = createMapper()

        private fun createMapper(): XmlMapper {
            val module = JacksonXmlModule().apply {
                setDefaultUseWrapper(false)
                addSerializer(Submission::class.java, SubmissionSerializer())
                addSerializer(Attribute::class.java, AttributeSerializer())
                addSerializer(AttrListSerializer(createType(List::class.java, Attribute::class.java)))
            }

            return XmlMapper(module).apply {
                setDefaultUseWrapper(false)
                setSerializationInclusion(NON_NULL)
                setSerializationInclusion(NON_EMPTY)
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            }
        }

        private fun createType(collectionClass: Class<*>, elementClass: Class<*>): JavaType {
            return TypeFactory.defaultInstance().constructCollectionLikeType(collectionClass, elementClass)
        }
    }
}
