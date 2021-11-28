package ac.uk.ebi.biostd.common

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.serialization.extensions.tryConvertValue

@ExtendWith(MockKExtension::class)
class ObjectMapperExtensionsTest(
    @MockK val jsonNode: JsonNode,
    @MockK val javaType: JavaType
) {

    @SpyK
    var testInstance: ObjectMapper = ObjectMapper()

    @Test
    fun tryToConvertWhenError() {
        every { testInstance.convertValue<Any>(jsonNode, javaType) } throws IllegalStateException()

        val result = testInstance.tryConvertValue(jsonNode, javaType)
        assertThat(result).isNull()
    }

    @Test
    fun tryToConvert() {
        every { testInstance.convertValue<Any>(jsonNode, javaType) } returns "a value"

        val result = testInstance.tryConvertValue(jsonNode, javaType)
        assertThat(result).isEqualTo("a value")
    }
}
