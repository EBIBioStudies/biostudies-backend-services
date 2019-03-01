package ac.uk.ebi.biostd.submission.converters

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ebi.ac.uk.io.asString
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SUBMISSION_TYPE
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_XML
import org.springframework.http.converter.HttpMessageConverter
import kotlin.reflect.full.isSuperclassOf

class PagetabConverter(private val serializerService: SerializationService) : HttpMessageConverter<Submission> {

    override fun canRead(clazz: Class<*>, mediaType: MediaType?) = Submission::class.isSuperclassOf(clazz.kotlin)

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?) = Submission::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes() = listOf(APPLICATION_JSON, TEXT_PLAIN, TEXT_XML)

    override fun write(submission: Submission, contentType: MediaType?, message: HttpOutputMessage) =
        message.body.write(serializerService.serializeSubmission(submission, asFormat(contentType)).toByteArray())

    override fun read(clazz: Class<out Submission>, message: HttpInputMessage) =
        serializerService.deserializeSubmission(
            message.body.asString(),
            asFormat(message.headers[SUBMISSION_TYPE].orEmpty()))

    private fun asFormat(mediaTypes: List<String>) =
            when {
            mediaTypes.contains(ebi.ac.uk.model.constants.APPLICATION_JSON) -> SubFormat.JSON
            mediaTypes.contains(ebi.ac.uk.model.constants.TEXT_PLAIN) -> SubFormat.TSV
            mediaTypes.contains(ebi.ac.uk.model.constants.TEXT_XML) -> SubFormat.XML
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
