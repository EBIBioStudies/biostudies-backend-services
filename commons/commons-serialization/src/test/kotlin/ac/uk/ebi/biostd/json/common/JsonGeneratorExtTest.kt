package ac.uk.ebi.biostd.json.common

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.StringWriter

internal class JsonGeneratorExtTest {

    private val objectMapper = ObjectMapper()
    private lateinit var target: StringWriter
    private lateinit var jsonGenerator: JsonGenerator

    @BeforeEach
    fun beforeEach() {
        target = StringWriter()
        jsonGenerator = JsonFactory().createGenerator(target)
        jsonGenerator.codec = objectMapper
    }

    @Test
    fun `writeObj with simple properties`() {
        jsonGenerator.run {
            writeObj {
                writeJsonString("string property", "string")
                writeJsonBoolean("boolean property", true)
                writeJsonNumber("number property", 55)
            }
            close()
        }

        assertThat(target.toString())
            .isEqualTo("{\"string property\":\"string\",\"boolean property\":true,\"number property\":55}")
    }

    @Test
    fun `writeObj with other`() {
        jsonGenerator.run {
            writeObj {
                writeJsonObject("object property", mapOf(Pair("a", 1)))
            }
            close()
        }

        assertThat(target.toString())
            .isEqualTo("{\"object property\":{\"a\":1}}")
    }
}
