package ac.uk.ebi.biostd.submission.converters

import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_XML
import org.springframework.http.converter.HttpMessageConverter
import uk.ac.ebi.biostd.integration.SerializationService
import uk.ac.ebi.biostd.integration.SubFormat
import uk.ac.ebi.biostd.model.Submission
import kotlin.reflect.full.isSuperclassOf

private val mediaTypes = listOf(APPLICATION_JSON, TEXT_PLAIN, TEXT_XML)

class JsonPagetabConverter(
    private val serializerService: SerializationService,
) :
    HttpMessageConverter<Submission> {
    override fun canRead(
        clazz: Class<*>,
        mediaType: MediaType?,
    ) = false

    override fun canWrite(
        clazz: Class<*>,
        mediaType: MediaType?,
    ) = Submission::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes() = mediaTypes

    override fun write(
        submission: Submission,
        contentType: MediaType?,
        message: HttpOutputMessage,
    ) {
        message.headers.contentType = APPLICATION_JSON
        message.body.write(serializerService.serializeSubmission(submission, SubFormat.JSON_PRETTY).toByteArray())
    }

    override fun read(
        clazz: Class<out Submission>,
        message: HttpInputMessage,
    ) = throw NotImplementedError()
}
