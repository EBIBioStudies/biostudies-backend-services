package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.json.exception.NoAttributeValueException
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AttributeDetailDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize attribute detail`() {
        val result =
            testInstance.readValue<AttributeDetail>(
                """{
                |"name": "attr name",
                |"value": "attr value"
                |}
                """.trimMargin(),
            )

        assertThat(result.name).isEqualTo("attr name")
        assertThat(result.value).isEqualTo("attr value")
    }

    @Test
    fun `deserialize empty`() {
        val exception = assertThrows<IllegalStateException> { testInstance.readValue<AttributeDetail>("{}") }
        assertThat(exception.message).isEqualTo("Expecting to find property with 'name' in node '{}'")
    }

    @Test
    fun `deserialize with no value property`() {
        val exception =
            assertThrows<IllegalStateException> {
                testInstance.readValue<AttributeDetail>(
                    """{
                    |"name": "attr name"
                    |}
                    """.trimMargin(),
                )
            }

        assertThat(exception.message).isEqualTo(
            "Expecting to find property with 'value' in node '{\"name\":\"attr name\"}'",
        )
    }

    @Test
    fun `deserialize no value`() {
        val exception =
            assertThrows<NoAttributeValueException> {
                testInstance.readValue<AttributeDetail>(
                    """{
                    |"name": "attr name",
                    |"value": ""
                    |}
                    """.trimMargin(),
                )
            }

        assertThat(exception.message).isEqualTo("The value for the attribute 'attr name' can't be empty")
    }
}
