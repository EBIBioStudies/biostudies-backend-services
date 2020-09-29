package ac.uk.ebi.biostd.submission.converters

import ac.uk.ebi.biostd.submission.web.model.ExtPage
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.reflect.full.isSuperclassOf

class ExtPageSubmissionConverter(
    private val extSerializationService: ExtSerializationService
) : HttpMessageConverter<ExtPage> {
    override fun canRead(clazz: Class<*>, mediaType: MediaType): Boolean = false

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean = ExtPage::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes(): List<MediaType> = listOf(MediaType.APPLICATION_JSON)

    override fun write(extSubmission: ExtPage, contentType: MediaType, message: HttpOutputMessage) {
        message.headers.contentType = MediaType.APPLICATION_JSON
        message.body.write(extSerializationService.serialize(extSubmission).toByteArray())
    }

    override fun read(clazz: Class<out ExtPage>, inputMessage: HttpInputMessage): ExtPage =
        throw NotImplementedError("ExtPage as input is not supported")
}
