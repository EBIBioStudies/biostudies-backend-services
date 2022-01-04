package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.extended.model.WebExtPage
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.reflect.full.isSuperclassOf

class ExtPageSubmissionConverter(
    private val extSerializationService: ExtSerializationService
) : HttpMessageConverter<WebExtPage> {
    override fun canRead(clazz: Class<*>, mediaType: MediaType): Boolean = false

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean =
        WebExtPage::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes(): List<MediaType> = listOf(MediaType.APPLICATION_JSON)

    override fun write(webExtPage: WebExtPage, contentType: MediaType, message: HttpOutputMessage) {
        message.headers.contentType = MediaType.APPLICATION_JSON
        message.body.write(extSerializationService.serialize(webExtPage).toByteArray())
    }

    override fun read(clazz: Class<out WebExtPage>, inputMessage: HttpInputMessage): WebExtPage =
        throw NotImplementedError("ExtPage as input is not supported")
}
