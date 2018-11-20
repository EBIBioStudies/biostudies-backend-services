package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.common.EitherDeserializer
import ac.uk.ebi.biostd.common.EitherSerializer
import ac.uk.ebi.biostd.json.deserialization.AttributeJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.FilesTableJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.LinksTableJsonDeserializer
import ac.uk.ebi.biostd.json.deserialization.SectionsTableJsonDeserializer
import ac.uk.ebi.biostd.json.serialization.AttributeJsonSerializer
import ac.uk.ebi.biostd.json.serialization.SubmissionJsonSerializer
import ac.uk.ebi.biostd.json.serialization.TableJsonSerializer
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.Table

class JsonSerializer {

    fun <T> serialize(t: T): String {
        return mapper.writeValueAsString(t)
    }

    fun <T> deserialize(value: String, valueType: Class<T>): T {
        return mapper.readValue(value, valueType)
    }

    companion object {
        val mapper = createMapper()

        private fun createMapper(): ObjectMapper {
            // TODO: add serializer and deserializer for each entity type to avoid issues when property name changed

            val module = SimpleModule().apply {
                addSerializer(Submission::class.java, SubmissionJsonSerializer())
                addSerializer(Attribute::class.java, AttributeJsonSerializer())
                addSerializer(Either::class.java, EitherSerializer())
                addSerializer(Table::class.java, TableJsonSerializer())

                addDeserializer(Attribute::class.java, AttributeJsonDeserializer())
                addDeserializer(Either::class.java, EitherDeserializer())
                addDeserializer(LinksTable::class.java, LinksTableJsonDeserializer())
                addDeserializer(FilesTable::class.java, FilesTableJsonDeserializer())
                addDeserializer(SectionsTable::class.java, SectionsTableJsonDeserializer())
            }

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
