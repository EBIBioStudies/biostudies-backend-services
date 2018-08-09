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
            val module = SimpleModule()
            module.addSerializer(Either::class.java, EitherSerializer())
            module.addSerializer(Submission::class.java, SubmissionSerializer())

            val mapper = XmlMapper()
            mapper.registerModule(module)
            mapper.setSerializationInclusion(NON_NULL)
            mapper.setSerializationInclusion(NON_EMPTY)
            mapper.enable(SerializationFeature.INDENT_OUTPUT)
            mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            return mapper
        }
    }
}
