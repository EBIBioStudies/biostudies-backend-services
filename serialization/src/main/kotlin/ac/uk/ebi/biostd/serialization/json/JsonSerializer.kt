package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.common.FilesTable
import ac.uk.ebi.biostd.common.LinksTable
import ac.uk.ebi.biostd.common.SectionsTable
import ac.uk.ebi.biostd.common.Table
import ac.uk.ebi.biostd.serialization.common.EitherSerializer
import ac.uk.ebi.biostd.serialization.common.EitherDeserializer
import ac.uk.ebi.biostd.submission.Attribute
import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonSerializer {
    fun <T> serialize(t: T): String {
        return mapper.writeValueAsString(t)
    }

    fun <T> deserialize(value: String, valueType: Class<T>): T {
        return mapper.readValue(value, valueType)
    }

    companion object {
        val mapper = JsonSerializer.createMapper()

        private fun createMapper(): ObjectMapper {
            val module = SimpleModule().apply {
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
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            }
        }
    }
}
