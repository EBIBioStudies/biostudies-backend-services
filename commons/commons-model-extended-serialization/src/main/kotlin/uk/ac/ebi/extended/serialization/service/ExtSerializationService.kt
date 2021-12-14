package uk.ac.ebi.extended.serialization.service

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import uk.ac.ebi.extended.serialization.deserializers.EitherExtTypeDeserializer
import uk.ac.ebi.extended.serialization.deserializers.ExtFileDeserializer
import uk.ac.ebi.extended.serialization.deserializers.ExtFileListDeserializer
import uk.ac.ebi.extended.serialization.deserializers.ExtFilesTableDeserializer
import uk.ac.ebi.extended.serialization.deserializers.ExtLinkDeserializer
import uk.ac.ebi.extended.serialization.deserializers.ExtLinksTableDeserializer
import uk.ac.ebi.extended.serialization.deserializers.ExtSectionsTableDeserializer
import uk.ac.ebi.extended.serialization.deserializers.OffsetDateTimeDeserializer
import uk.ac.ebi.extended.serialization.serializers.ExtFileSerializer
import uk.ac.ebi.extended.serialization.serializers.ExtFilesTableSerializer
import uk.ac.ebi.extended.serialization.serializers.ExtLinkSerializer
import uk.ac.ebi.extended.serialization.serializers.ExtLinksTableSerializer
import uk.ac.ebi.extended.serialization.serializers.ExtSectionSerializer
import uk.ac.ebi.extended.serialization.serializers.ExtSectionsTableSerializer
import uk.ac.ebi.extended.serialization.serializers.ExtSubmissionSerializer
import uk.ac.ebi.extended.serialization.serializers.OffsetDateTimeSerializer
import uk.ac.ebi.serialization.serializers.EitherSerializer
import java.io.InputStream
import java.io.OutputStream
import java.io.StringWriter
import java.time.OffsetDateTime

data class Properties(val includeFileListFiles: Boolean) : StringWriter()

class ExtSerializationService {

    fun serializeFileList(files: Sequence<ExtFile>, outputStream: OutputStream) {
        val jsonGenerator = mapper.factory.createGenerator(outputStream)

        jsonGenerator.use {
            it.writeStartArray()
            files.forEach { file -> mapper.writeValue(it, file) }
            it.writeEndArray()
        }
    }

    fun deserializeFileList(inputStream: InputStream): Sequence<ExtFile> {
        val jsonParser = mapper.factory.createParser(inputStream)

        if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
            throw IllegalStateException("Expected content to be an array")
        }

        return sequence {
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                yield(mapper.readValue(jsonParser, ExtFile::class.java))
            }
        }
    }

    fun <T> serialize(element: T, properties: Properties = Properties(false)): String {
        mapper.writerWithDefaultPrettyPrinter().writeValue(properties, element)
        return properties.buffer.toString()
    }

    inline fun <reified T> deserialize(value: String): T = mapper.readValue(value)

    fun <T> deserialize(value: String, type: Class<out T>): T = mapper.readValue(value, type)

    companion object {
        val mapper = createMapper()

        private fun createMapper(): ObjectMapper {
            val module = SimpleModule().apply {
                addDeserializer(Either::class.java, EitherExtTypeDeserializer())
                addDeserializer(ExtFile::class.java, ExtFileDeserializer())
                addDeserializer(ExtFileTable::class.java, ExtFilesTableDeserializer())
                addDeserializer(ExtLink::class.java, ExtLinkDeserializer())
                addDeserializer(ExtFileList::class.java, ExtFileListDeserializer())
                addDeserializer(ExtLinkTable::class.java, ExtLinksTableDeserializer())
                addDeserializer(ExtSectionTable::class.java, ExtSectionsTableDeserializer())
                addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer())

                addSerializer(Either::class.java, EitherSerializer())
                addSerializer(ExtFile::class.java, ExtFileSerializer())
                addSerializer(ExtFileTable::class.java, ExtFilesTableSerializer())
                addSerializer(ExtLink::class.java, ExtLinkSerializer())
                addSerializer(ExtLinkTable::class.java, ExtLinksTableSerializer())
                addSerializer(ExtSection::class.java, ExtSectionSerializer())
                addSerializer(ExtSubmission::class.java, ExtSubmissionSerializer())
                addSerializer(ExtSectionTable::class.java, ExtSectionsTableSerializer())
                addSerializer(OffsetDateTime::class.java, OffsetDateTimeSerializer())
            }

            return jacksonObjectMapper().apply {
                registerModule(module)
                registerKotlinModule()
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
