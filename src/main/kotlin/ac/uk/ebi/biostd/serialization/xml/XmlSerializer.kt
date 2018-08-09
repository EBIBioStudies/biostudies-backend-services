package ac.uk.ebi.biostd.serialization.xml

import ac.uk.ebi.biostd.serialization.xml.serializer.EitherSerializer
import ac.uk.ebi.biostd.submission.Submission
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper


class XmlSerializer {

    fun serialize(submission: Submission): String {
        return mapper.writeValueAsString(submission)
    }

    companion object {
        val mapper = createMapper()

        private fun createMapper(): XmlMapper {
            val module = SimpleModule()
            module.addSerializer(Either::class.java, EitherSerializer())

            val mapper = XmlMapper()
            mapper.registerModule(module)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            return mapper
        }
    }
}
