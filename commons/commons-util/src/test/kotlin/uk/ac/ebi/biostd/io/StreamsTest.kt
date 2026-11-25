package uk.ac.ebi.biostd.io

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

internal class StreamsTest {
    @Test
    fun use() {
        val input = "one".byteInputStream()
        val output = ByteArrayOutputStream()
        use(input, output) { inStream, outStream -> outStream.write(inStream.readAllBytes()) }

        assertThat(output.toString()).isEqualTo("one")
    }
}
