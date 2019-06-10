package ac.uk.ebi.biostd.submission.converters

import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.JSON
import ac.uk.ebi.biostd.integration.SubFormat.XML
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_XML
import org.springframework.http.converter.HttpMessageConverter
import kotlin.reflect.full.isSuperclassOf

class PagetabConverter(private val serializerService: ISerializationService) : HttpMessageConverter<Submission> {

    override fun canRead(clazz: Class<*>, mediaType: MediaType?) = false

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?) = Submission::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes() = listOf(APPLICATION_JSON, TEXT_PLAIN, TEXT_XML)

    override fun write(submission: Submission, contentType: MediaType?, message: HttpOutputMessage) =
        message.body.write(serializerService.serializeSubmission(submission, asFormat(contentType)).toByteArray())

    override fun read(clazz: Class<out Submission>, message: HttpInputMessage) = throw NotImplementedError()

    private fun asFormat(mediaType: MediaType?) =
        when (mediaType) {
            APPLICATION_JSON -> JSON
            TEXT_PLAIN -> SubFormat.TSV
            TEXT_XML -> XML
            else -> JSON
        }
}
