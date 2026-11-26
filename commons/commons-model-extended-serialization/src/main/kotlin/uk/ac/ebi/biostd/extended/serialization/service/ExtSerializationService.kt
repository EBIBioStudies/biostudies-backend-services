package uk.ac.ebi.biostd.extended.serialization.service

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.flow.Flow
import uk.ac.ebi.biostd.base.Either
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtFileList
import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtLinkList
import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.extended.model.ExtPage
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSectionTable
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.WebExtPage
import uk.ac.ebi.biostd.extended.serialization.deserializers.EitherExtTypeDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtAttributeDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtFileDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtFileListDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtFilesTableDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtLinkDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtLinkListDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtLinksTableDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtSectionDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.ExtSectionsTableDeserializer
import uk.ac.ebi.biostd.extended.serialization.deserializers.OffsetDateTimeDeserializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtAttributeSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtFileSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtFilesTableSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtLinkSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtLinksTableSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtSectionSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtSectionsTableSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.ExtSubmissionSerializer
import uk.ac.ebi.biostd.extended.serialization.serializers.OffsetDateTimeSerializer
import uk.ac.ebi.biostd.serialization.extensions.deserializeAsFlow
import uk.ac.ebi.biostd.serialization.extensions.deserializeAsSequence
import uk.ac.ebi.biostd.serialization.extensions.serializeFlow
import uk.ac.ebi.biostd.serialization.extensions.serializeList
import uk.ac.ebi.biostd.serialization.serializers.EitherSerializer
import java.io.InputStream
import java.io.OutputStream
import java.io.StringWriter
import java.time.OffsetDateTime

data class Properties(
    val includeFileListFiles: Boolean,
    val includeLinkListLinks: Boolean,
) : StringWriter()

@Suppress("TooManyFunctions")
class ExtSerializationService private constructor(
    val mapper: ObjectMapper,
) {
    fun serialize(
        sub: ExtSubmission,
        props: Properties = Properties(includeFileListFiles = false, includeLinkListLinks = false),
    ): String = serializeElement(sub, props)

    fun serialize(
        files: Sequence<ExtFile>,
        stream: OutputStream,
    ): Int = mapper.serializeList(files, stream)

    fun serialize(file: ExtFile): String = serializeElement(file)

    fun serialize(table: ExtFileTable): String = serializeElement(table)

    fun <T> serialize(extPage: WebExtPage<T>): String = serializeElement(extPage)

    suspend fun serialize(
        files: Flow<ExtFile>,
        stream: OutputStream,
    ): Int = mapper.serializeFlow(files, stream)

    suspend fun serializeLinks(
        links: Flow<ExtLink>,
        stream: OutputStream,
    ): Int = mapper.serializeFlow(links, stream)

    fun deserialize(value: String): ExtSubmission = mapper.readValue(value)

    fun deserializeFile(value: String): ExtFile = mapper.readValue(value)

    fun deserializePage(value: String): ExtPage = mapper.readValue(value)

    // TODO unused
    fun deserializeTable(value: String): ExtFileTable = mapper.readValue(value)

    fun deserializeFileListAsSequence(stream: InputStream): Sequence<ExtFile> = mapper.deserializeAsSequence(stream)

    fun deserializeLinkListAsSequence(stream: InputStream): Sequence<ExtLink> = mapper.deserializeAsSequence(stream)

    fun deserializeFileListAsFlow(stream: InputStream): Flow<ExtFile> = mapper.deserializeAsFlow(stream)

    fun deserializeLinkListAsFlow(stream: InputStream): Flow<ExtLink> = mapper.deserializeAsFlow(stream)

    /**
     * Serialize a generic element. ONLY for testing purpose.
     */
    internal fun <T> serializeElement(
        element: T,
        properties: Properties = Properties(includeFileListFiles = false, includeLinkListLinks = false),
    ): String {
        mapper.writerWithDefaultPrettyPrinter().writeValue(properties, element)
        return properties.buffer.toString()
    }

    companion object {
        operator fun invoke(): ExtSerializationService = instance

        val mapper = createMapper()
        private val instance = ExtSerializationService(mapper)

        fun createMapper(): ObjectMapper {
            val module =
                SimpleModule().apply {
                    addDeserializer(ExtSection::class.java, ExtSectionDeserializer())
                    addDeserializer(Either::class.java, EitherExtTypeDeserializer())
                    addDeserializer(ExtAttribute::class.java, ExtAttributeDeserializer())
                    addDeserializer(ExtFile::class.java, ExtFileDeserializer())
                    addDeserializer(ExtFileTable::class.java, ExtFilesTableDeserializer())
                    addDeserializer(ExtLink::class.java, ExtLinkDeserializer())
                    addDeserializer(ExtFileList::class.java, ExtFileListDeserializer())
                    addDeserializer(ExtLinkList::class.java, ExtLinkListDeserializer())
                    addDeserializer(ExtLinkTable::class.java, ExtLinksTableDeserializer())
                    addDeserializer(ExtSectionTable::class.java, ExtSectionsTableDeserializer())
                    addDeserializer(OffsetDateTime::class.java, OffsetDateTimeDeserializer())

                    addSerializer(Either::class.java, EitherSerializer())
                    addSerializer(ExtAttribute::class.java, ExtAttributeSerializer())
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
                registerModule(JavaTimeModule())
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
