package ac.uk.ebi.biostd.submission.converters

import ac.uk.ebi.biostd.extended.ExtSubmissionSerializer
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.converter.HttpMessageConverter
import kotlin.reflect.full.isSuperclassOf

class ExtSubmissionConverter(
    private val extSubmissionSerializer: ExtSubmissionSerializer
) : HttpMessageConverter<ExtSubmission> {
    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canWrite(
        clazz: Class<*>,
        mediaType: MediaType?
    ): Boolean = ExtSubmission::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes(): List<MediaType> = listOf(APPLICATION_JSON)

    override fun write(extSubmission: ExtSubmission, contentType: MediaType?, message: HttpOutputMessage) {
        message.headers.contentType = APPLICATION_JSON
        message.body.write(extSubmissionSerializer.serialize(extSubmission).toByteArray())
    }

    override fun read(clazz: Class<out ExtSubmission>, inputMessage: HttpInputMessage): ExtSubmission {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
