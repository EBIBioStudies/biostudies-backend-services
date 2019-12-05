package ebi.ac.uk.model.constants

import ebi.ac.uk.errors.UnsupportedStatusException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProcessingStatusTest {
    @Test
    fun `value of processed`() {
        assertThat(ProcessingStatus.valueOf("PROCESSED")).isEqualTo(Processed)
    }

    @Test
    fun `value of processing`() {
        assertThat(ProcessingStatus.valueOf("PROCESSING")).isEqualTo(Processing)
    }

    @Test
    fun `unsupported status`() {
        val exception = assertThrows<UnsupportedStatusException> { ProcessingStatus.valueOf("LOST") }
        assertThat(exception.message).isEqualTo("Unsupported status: LOST")
    }
}
