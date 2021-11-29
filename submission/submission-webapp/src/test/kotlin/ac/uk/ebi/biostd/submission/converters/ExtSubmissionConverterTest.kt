package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.Properties
import java.io.OutputStream

@ExtendWith(MockKExtension::class)
class ExtSubmissionConverterTest(@MockK private val extSerializationService: ExtSerializationService) {
    private val testInstance = ExtSubmissionConverter(extSerializationService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `can read`() {
        assertThat(testInstance.canRead(ExtSubmission::class.java, APPLICATION_JSON)).isTrue
    }

    @Test
    fun `can not read`() {
        assertThat(testInstance.canRead(Submission::class.java, APPLICATION_JSON)).isFalse
    }

    @Test
    fun `can write`() {
        assertThat(testInstance.canWrite(ExtSubmission::class.java, APPLICATION_JSON)).isTrue
    }

    @Test
    fun `can not write`() {
        assertThat(testInstance.canWrite(Submission::class.java, APPLICATION_JSON)).isFalse
    }

    @Test
    fun `supported media types`() {
        val mediaTypes = testInstance.supportedMediaTypes
        assertThat(mediaTypes).hasSize(1)
        assertThat(mediaTypes.first()).isEqualTo(APPLICATION_JSON)
    }

    @Test
    fun write(
        @MockK message: HttpOutputMessage,
        @MockK extSubmission: ExtSubmission,
        @MockK(relaxed = true) body: OutputStream,
        @MockK(relaxed = true) headers: HttpHeaders
    ) {
        every { message.body } returns body
        every { message.headers } returns headers
        every { extSerializationService.serialize(extSubmission, Properties(false)) } returns "submission"

        testInstance.write(extSubmission, APPLICATION_JSON, message)

        verify { headers.contentType = APPLICATION_JSON }
        verify { body.write("submission".toByteArray()) }
        verify { extSerializationService.serialize(extSubmission, any()) }
    }

    @Test
    fun read(
        @MockK message: HttpInputMessage,
        @MockK extSubmission: ExtSubmission
    ) {
        every { message.body } returns "submission".byteInputStream()
        every { extSerializationService.deserialize("submission", ExtSubmission::class.java) } returns extSubmission

        val read = testInstance.read(ExtSubmission::class.java, message)
        assertThat(read).isEqualTo(extSubmission)
    }
}
