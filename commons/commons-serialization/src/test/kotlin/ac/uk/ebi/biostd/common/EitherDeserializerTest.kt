package ac.uk.ebi.biostd.common

import arrow.core.Either
import arrow.core.getOrHandle
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import uk.ac.ebi.serialization.deserializers.EitherDeserializer

@TestInstance(Lifecycle.PER_CLASS)
class EitherDeserializerTest {
    private val objectMapper = jacksonObjectMapper()

    @BeforeAll
    fun beforeAll() {
        objectMapper.registerModule(SimpleModule().apply { addDeserializer(Either::class.java, EitherDeserializer()) })
    }

    @Test
    fun deserializeWhenLeft() {
        val name = "John Doe"
        val result = objectMapper.readValue<Either<Dummy, Foo>>(jsonObj { "name" to name }.toString())

        assertThat(result.isLeft()).isTrue
        assertThat(result.getOrHandle { it }).isEqualTo(Dummy(name))
    }

    @Test
    fun deserializeWhenRight() {
        val value = 55
        val result = objectMapper.readValue<Either<Dummy, Foo>>(jsonObj { "value" to value }.toString())

        assertThat(result.isRight()).isTrue
        assertThat(result.getOrHandle { it }).isEqualTo(Foo(value))
    }

    @Test
    fun deserializeWhenList() {
        val name = "John Doe"

        val result = objectMapper.readValue<MutableList<Either<Dummy, Foo>>>(jsonArray({ "name" to name }).toString())

        assertThat(result).isNotEmpty
        assertThat(result.first().isLeft())
        assertThat(result.first().getOrHandle { it }).isEqualTo(Dummy(name))
    }

    data class Dummy(var name: String?)

    data class Foo(var value: Int?)
}
