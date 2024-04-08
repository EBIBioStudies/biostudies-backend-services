package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.ext.asString
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.converter.HttpMessageConverter
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.reflect.full.isSuperclassOf

class ExtSubmissionConverter(
    private val extSerializationService: ExtSerializationService,
) : HttpMessageConverter<ExtSubmission> {
    override fun canRead(
        clazz: Class<*>,
        mediaType: MediaType?,
    ): Boolean = ExtSubmission::class.isSuperclassOf(clazz.kotlin)

    override fun canWrite(
        clazz: Class<*>,
        mediaType: MediaType?,
    ): Boolean = ExtSubmission::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes(): List<MediaType> = listOf(APPLICATION_JSON)

    override fun write(
        extSubmission: ExtSubmission,
        contentType: MediaType?,
        message: HttpOutputMessage,
    ) {
        message.headers.contentType = APPLICATION_JSON
        message.body.write(extSerializationService.serialize(extSubmission).toByteArray())
    }

    override fun read(
        clazz: Class<out ExtSubmission>,
        message: HttpInputMessage,
    ): ExtSubmission = extSerializationService.deserialize(message.body.asString())
}
