package ac.uk.ebi.biostd.rest.config

import ac.uk.ebi.biostd.json.JsonSerializer
import ebi.ac.uk.io.asString
import ebi.ac.uk.model.Submission
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.converter.HttpMessageConverter
import kotlin.reflect.full.isSuperclassOf

class JsonPagetabConverter(private val jsonSerializer: JsonSerializer) : HttpMessageConverter<Submission> {

    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return Submission::class.isSuperclassOf(clazz.kotlin).and(mediaType == APPLICATION_JSON)
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return Submission::class.isSuperclassOf(clazz.kotlin).and(mediaType == APPLICATION_JSON)
    }

    override fun getSupportedMediaTypes() = mutableListOf(APPLICATION_JSON)

    override fun write(submission: Submission, contentType: MediaType?, outputMessage: HttpOutputMessage) {
        outputMessage.body.use {
            it.write(jsonSerializer.serialize(submission).toByteArray())
        }
    }

    override fun read(clazz: Class<out Submission>, inputMessage: HttpInputMessage) =
            jsonSerializer.deserialize(inputMessage.body.asString(), Submission::class.java)
}