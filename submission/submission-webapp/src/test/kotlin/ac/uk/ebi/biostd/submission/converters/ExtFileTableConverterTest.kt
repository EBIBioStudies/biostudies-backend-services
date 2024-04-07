package ac.uk.ebi.biostd.submission.converters

import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpInputMessage
import org.springframework.http.HttpOutputMessage
import org.springframework.http.MediaType.APPLICATION_JSON
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.OutputStream

@ExtendWith(MockKExtension::class)
class ExtFileTableConverterTest(
    @MockK private val extSerializationService: ExtSerializationService,
) {
    private val testInstance = ExtFileTableConverter(extSerializationService)

    @Test
    fun `can read`() {
        assertThat(testInstance.canRead(ExtFileTable::class.java, APPLICATION_JSON)).isFalse
    }

    @Test
    fun `can write ext file table`() {
        assertThat(testInstance.canWrite(ExtFileTable::class.java, APPLICATION_JSON)).isTrue
    }

    @Test
    fun `can write other class`() {
        assertThat(testInstance.canWrite(ExtSubmission::class.java, APPLICATION_JSON)).isFalse
    }

    @Test
    fun read(
        @MockK input: HttpInputMessage,
    ) {
        val exception = assertThrows<NotImplementedError> { testInstance.read(ExtFileTable::class.java, input) }
        assertThat(exception.message).isEqualTo("ExtFileTable as input is not supported")
    }

    @Test
    fun write(
        @MockK extFileTable: ExtFileTable,
        @MockK message: HttpOutputMessage,
        @MockK(relaxed = true) body: OutputStream,
        @MockK(relaxed = true) headers: HttpHeaders,
    ) {
        every { message.body } returns body
        every { message.headers } returns headers
        every { extSerializationService.serialize(extFileTable) } returns "ExtFileTable"

        testInstance.write(extFileTable, APPLICATION_JSON, message)

        verify(exactly = 1) { headers.contentType = APPLICATION_JSON }
        verify(exactly = 1) { body.write("ExtFileTable".toByteArray()) }
        verify(exactly = 1) { extSerializationService.serialize(extFileTable) }
    }
}
