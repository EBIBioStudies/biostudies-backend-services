package ac.uk.ebi.biostd.serialization.xml

import ac.uk.ebi.biostd.serialization.common.EitherSerializer
import ac.uk.ebi.biostd.serialization.xml.serializer.SubmissionSerializer
import ac.uk.ebi.biostd.submission.Submission
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class XmlSerializer {

    fun serialize(submission: Submission): String {
        return mapper.writeValueAsString(submission)
    }

    companion object {
        val mapper = createMapper()

        private fun createMapper(): XmlMapper {
            val module = SimpleModule().apply {
                addSerializer(Either::class.java, EitherSerializer())
                addSerializer(Submission::class.java, SubmissionSerializer())
            }

            return XmlMapper().apply {
                registerModule(module)
                setSerializationInclusion(NON_NULL)
                setSerializationInclusion(NON_EMPTY)
                enable(SerializationFeature.INDENT_OUTPUT)
                configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            }
        }
    }
}
