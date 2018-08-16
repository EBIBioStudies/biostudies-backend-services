package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.common.*
import ac.uk.ebi.biostd.serialization.common.EitherSerializer
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
            val module = SimpleModule()
            module.addSerializer(Attribute::class.java, AttributeJsonSerializer())
            module.addSerializer(Either::class.java, EitherSerializer())
            module.addSerializer(Table::class.java, TableJsonSerializer())

            module.addDeserializer(Attribute::class.java, AttributeJsonDeserializer())
            module.addDeserializer(LinksTable::class.java, LinksTableJsonDeserializer())
            module.addDeserializer(FilesTable::class.java, FilesTableJsonDeserializer())
            module.addDeserializer(SectionsTable::class.java, SectionsTableJsonDeserializer())

            val mapper = jacksonObjectMapper()
            mapper.registerModule(module)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            return mapper
        }
    }
}
