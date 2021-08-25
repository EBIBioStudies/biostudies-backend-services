package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.extended.model.ExtFileTable
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.converter.HttpMessageConverter
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import kotlin.reflect.full.isSuperclassOf

class ExtFileTableConverter(
    private val extSerializationService: ExtSerializationService
) : HttpMessageConverter<ExtFileTable> {
    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean = false

    override fun canWrite(
        clazz: Class<*>,
        mediaType: MediaType?
    ): Boolean = ExtFileTable::class.isSuperclassOf(clazz.kotlin)

    override fun getSupportedMediaTypes(): List<MediaType> = listOf(APPLICATION_JSON)

    override fun read(
        clazz: Class<out ExtFileTable>,
        inputMessage: HttpInputMessage
    ): ExtFileTable = throw NotImplementedError("ExtFileTable as input is not supported")

    override fun write(extFileTable: ExtFileTable, contentType: MediaType?, message: HttpOutputMessage) {
        message.headers.contentType = APPLICATION_JSON
        message.body.write(extSerializationService.serialize(extFileTable).toByteArray())
    }
}
