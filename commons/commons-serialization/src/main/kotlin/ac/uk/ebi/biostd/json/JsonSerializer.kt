package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.common.EitherDeserializer
import ac.uk.ebi.biostd.common.EitherSerializer
import ac.uk.ebi.biostd.json.deserialization.AttributeJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.FileJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.FilesTableJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.LinkJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.LinksTableJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.SectionJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.SectionsTableJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.SubmissionJsonDeserializer
import ac.uk.ebi.biostd.json.serialization.AttributeJsonSerializer
import ac.uk.ebi.biostd.json.serialization.FileJsonSerializer
import ac.uk.ebi.biostd.json.serialization.LinkJsonSerializer
import ac.uk.ebi.biostd.json.serialization.SectionJsonSerializer
import ac.uk.ebi.biostd.json.serialization.SubmissionJsonSerializer
import ac.uk.ebi.biostd.json.serialization.TableJsonSerializer
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table

internal class JsonSerializer {
    fun <T> serialize(element: T, pretty: Boolean = false): String {
        return if (pretty)
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(element)
        else
            mapper.writeValueAsString(element)
    }

    inline fun <reified T> deserialize(value: String) = mapper.readValue<T>(value)

    fun <T> deserialize(value: String, type: Class<out T>) = mapper.readValue(value, type)

    companion object {
        val mapper = createMapper()

        private fun createMapper(): ObjectMapper {
            val module = SimpleModule().apply {
                addSerializer(Submission::class.java, SubmissionJsonSerializer())
                addSerializer(Section::class.java, SectionJsonSerializer())
                addSerializer(Link::class.java, LinkJsonSerializer())
                addSerializer(File::class.java, FileJsonSerializer())
                addSerializer(Attribute::class.java, AttributeJsonSerializer())
                addSerializer(Either::class.java, EitherSerializer())
                addSerializer(Table::class.java, TableJsonSerializer())

                addDeserializer(Submission::class.java, SubmissionJsonDeserializer())
                addDeserializer(Section::class.java, SectionJsonDeserializer())
                addDeserializer(Link::class.java, LinkJsonDeserializer())
                addDeserializer(File::class.java, FileJsonDeserializer())
                addDeserializer(Attribute::class.java, AttributeJsonDeserializer())
                addDeserializer(Either::class.java, EitherDeserializer())
                addDeserializer(LinksTable::class.java, LinksTableJsonDeserializer())
                addDeserializer(SectionsTable::class.java, SectionsTableJsonDeserializer())
                addDeserializer(FilesTable::class.java, FilesTableJsonDeserializer())
            }

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
