package ac.uk.ebi.biostd.rest.converters

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.io.asString
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_XML
import org.springframework.http.converter.HttpMessageConverter
import kotlin.reflect.full.isSuperclassOf

class PagetabConverter(private val serializerService: SerializationService) : HttpMessageConverter<Submission> {

    override fun canRead(clazz: Class<*>, mediaType: MediaType?) =
        Submission::class.isSuperclassOf(clazz.kotlin)

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?) =
        Submission::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes() = listOf(APPLICATION_JSON, TEXT_PLAIN, TEXT_XML)

    override fun write(submission: Submission, contentType: MediaType?, outputMessage: HttpOutputMessage) =
        outputMessage.body.write(serializerService.serializeSubmission(submission, asFormat(contentType)).toByteArray())

    override fun read(clazz: Class<out Submission>, inputMessage: HttpInputMessage) =
        serializerService.deserializeSubmission(inputMessage.body.asString(), asFormat(inputMessage.headers.accept))

    private fun asFormat(mediaType: List<MediaType>) =
        when {
            mediaType.contains(MediaType.APPLICATION_JSON) -> SubFormat.JSON
            mediaType.contains(MediaType.TEXT_PLAIN) -> SubFormat.TSV
            mediaType.contains(MediaType.TEXT_XML) -> SubFormat.XML
            else -> SubFormat.JSON
        }

    private fun asFormat(mediaType: MediaType?) =
        when (mediaType) {
            MediaType.APPLICATION_JSON -> SubFormat.JSON
            MediaType.TEXT_PLAIN -> SubFormat.TSV
            MediaType.TEXT_XML -> SubFormat.XML
            else -> SubFormat.JSON
        }
}