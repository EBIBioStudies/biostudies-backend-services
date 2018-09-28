package ac.uk.ebi.biostd.serialization.common

import arrow.core.Either
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.soundvibe.jkob.json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
class EitherSerializerTest {

    private val objectMapper = jacksonObjectMapper()

    @BeforeAll
    fun beforeAll() {
        objectMapper.registerModule(SimpleModule().apply { addSerializer(Either::class.java, EitherSerializer()) })
    }

    @Test
    fun serializeWhenLeft() {
        val name = "John Doe"
        val result = objectMapper.writeValueAsString(Either.left(Dummy(name)))

        assertThat(result).isEqualToIgnoringWhitespace(json { "name" to name }.toString())
    }

    @Test
    fun serializeWhenRight() {
        val name = "John Doe"
        val result = objectMapper.writeValueAsString(Either.right(Dummy(name)))

        assertThat(result).isEqualToIgnoringWhitespace(json { "name" to name }.toString())
    }

    data class Dummy(var name: String?)
}
