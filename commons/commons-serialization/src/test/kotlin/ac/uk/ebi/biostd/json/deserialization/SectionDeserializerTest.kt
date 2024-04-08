package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.section
import ebi.ac.uk.model.Section
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SectionDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize with wrong acc no type`() {
        val invalidJson =
            jsonObj {
                "accno" to jsonArray(1, 2, 3)
            }.toString()

        val node = "{\"accno\":[1,2,3]}"
        val exception = assertThrows<IllegalArgumentException> { testInstance.readValue<Section>(invalidJson) }
        assertThat(exception.message).isEqualTo(
            "Expecting node: '$node', property: 'accno' to be of type 'TextNode' but 'ArrayNode' was found instead",
        )
    }

    @Test
    fun `deserialize study submission`() {
        val json =
            jsonObj {
                "accno" to "abc123"
                "type" to "Study"
            }.toString()

        assertThat(testInstance.readValue<Section>(json)).isEqualTo(section("Study") { accNo = "abc123" })
    }

    @Test
    fun `deserialize with null acc number`() {
        val json =
            jsonObj {
                "accno" to null
                "type" to "Study"
            }.toString()

        assertThat(testInstance.readValue<Section>(json)).isEqualTo(section("Study") { })
    }

    @Test
    fun `deserialize generic type`() {
        val json =
            jsonObj {
                "accno" to "abc123"
                "type" to "Compound"
            }.toString()

        assertThat(testInstance.readValue<Section>(json)).isEqualTo(section("Compound") { accNo = "abc123" })
    }
}
