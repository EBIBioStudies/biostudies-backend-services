package ac.uk.ebi.biostd.common

import arrow.core.Either
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.json.jsonObj
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import uk.ac.ebi.serialization.serializers.EitherSerializer

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

        assertThat(result).isEqualToIgnoringWhitespace(jsonObj { "name" to name }.toString())
    }

    @Test
    fun serializeWhenRight() {
        val name = "John Doe"
        val result = objectMapper.writeValueAsString(Either.right(Dummy(name)))

        assertThat(result).isEqualToIgnoringWhitespace(jsonObj { "name" to name }.toString())
    }

    data class Dummy(var name: String?)
}
