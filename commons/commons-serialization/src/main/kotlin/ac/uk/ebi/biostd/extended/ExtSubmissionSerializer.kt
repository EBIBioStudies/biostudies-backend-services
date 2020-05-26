package ac.uk.ebi.biostd.extended

import ac.uk.ebi.biostd.common.EitherSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtFileListSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtFileSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtFilesTableSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtLinkSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtLinksTableSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtSectionSerializer
import ac.uk.ebi.biostd.extended.serialization.ExtSectionsTableSerializer
import ac.uk.ebi.biostd.extended.serialization.OffsetDateTimeSerializer
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import java.time.OffsetDateTime

class ExtSubmissionSerializer {
    fun <T> serialize(element: T): String =
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(element)

    inline fun <reified T> deserialize(value: String) = mapper.readValue<T>(value)

    fun <T> deserialize(value: String, type: Class<out T>) = mapper.readValue(value, type)

    companion object {
        val mapper = createMapper()

        private fun createMapper(): ObjectMapper {
            val module = SimpleModule().apply {
                addSerializer(Either::class.java, EitherSerializer())
                addSerializer(ExtFile::class.java, ExtFileSerializer())
                addSerializer(ExtFileList::class.java, ExtFileListSerializer())
                addSerializer(ExtFileTable::class.java, ExtFilesTableSerializer())
                addSerializer(ExtLink::class.java, ExtLinkSerializer())
                addSerializer(ExtLinkTable::class.java, ExtLinksTableSerializer())
                addSerializer(ExtSection::class.java, ExtSectionSerializer())
                addSerializer(ExtSectionTable::class.java, ExtSectionsTableSerializer())
                addSerializer(OffsetDateTime::class.java, OffsetDateTimeSerializer())
            }

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
