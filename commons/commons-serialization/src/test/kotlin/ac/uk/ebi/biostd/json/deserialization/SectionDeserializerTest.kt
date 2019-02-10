package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.ext.deserialize
import ac.uk.ebi.biostd.json.JsonSerializer
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Section
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class SectionDeserializerTest {

    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize with wrong acc no type`() {
        val invalidJson = jsonObj {
            "accNo" to jsonArray(1, 2, 3)
        }.toString()

        val exception = assertThrows<IllegalArgumentException> { testInstance.deserialize<Section>(invalidJson) }
        assertThat(exception.message).isEqualTo("Expecting node: '{\"accNo\":[1,2,3]}', property: 'accNo' to be of type 'TextNode' but 'ArrayNode' find instead")
    }

    @Test
    fun `deserialize`() {
        val json = jsonObj {
            "accNo" to "abc123"
            "type" to "Study"
        }.toString()

        val section = testInstance.deserialize<Section>(json)
        assertThat(section.accNo).isEqualTo("abc123")
        assertThat(section.attributes).isEmpty()
        assertThat(section.links).isEmpty()
        assertThat(section.files).isEmpty()
        assertThat(section.sections).isEmpty()
    }
}
