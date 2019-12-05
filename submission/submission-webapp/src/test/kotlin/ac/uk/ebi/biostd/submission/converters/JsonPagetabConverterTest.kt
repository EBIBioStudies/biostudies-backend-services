package ac.uk.ebi.biostd.submission.converters

import ac.uk.ebi.biostd.integration.SerializationService
import ebi.ac.uk.model.Submission
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.TEXT_PLAIN
import org.springframework.http.MediaType.TEXT_XML

@ExtendWith(MockKExtension::class)
internal class JsonPagetabConverterTest(@MockK private val serializationService: SerializationService) {

    private val testInstance = JsonPagetabConverter(serializationService)

    @Test
    fun canRead() {
        assertThat(testInstance.canRead(Submission::class.java, null)).isFalse()
    }

    @Test
    fun `canWrite when can`() {
        assertThat(testInstance.canWrite(Submission::class.java, null)).isTrue()
    }

    @Test
    fun `can Write when can not`() {
        assertThat(testInstance.canWrite(String::class.java, APPLICATION_JSON)).isTrue()
    }

    @Test
    fun `get supported media types`() {
        assertThat(testInstance.supportedMediaTypes).isEqualTo(listOf(APPLICATION_JSON, TEXT_PLAIN, TEXT_XML))
    }

    @Test
    fun write(@MockK message: HttpOutputMessage) {
    }

    @Test
    fun read() {
    }
}