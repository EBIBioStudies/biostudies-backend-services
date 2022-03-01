package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ExtAttributeSerializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `serialize attribute with empty value`() {
        val json = testInstance.writeValueAsString(ExtAttribute(name = "attr name", value = ""))
        val expected = jsonObj {
            "name" to "attr name"
            "value" to null
            "reference" to false
            "nameAttrs" to jsonArray()
            "valueAttrs" to jsonArray()
        }.toString()

        assertThat(json).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun `serialize attribute with null value`() {
        val json = testInstance.writeValueAsString(ExtAttribute(name = "attr name", value = null))
        val expected = jsonObj {
            "name" to "attr name"
            "value" to null
            "reference" to false
            "nameAttrs" to jsonArray()
            "valueAttrs" to jsonArray()
        }.toString()

        assertThat(json).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun `serialize attribute with blank value`() {
        val json = testInstance.writeValueAsString(ExtAttribute(name = "attr name", value = "  "))
        val expected = jsonObj {
            "name" to "attr name"
            "value" to null
            "reference" to false
            "nameAttrs" to jsonArray()
            "valueAttrs" to jsonArray()
        }.toString()

        assertThat(json).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun `serialize attribute with details and reference`() {
        val nameDetails = ExtAttributeDetail("t1", "v1")
        val valDetails = ExtAttributeDetail("t2", "v2")

        val attr = ExtAttribute(
            name = "attr name",
            value = "attr value",
            reference = true,
            nameAttrs = listOf(nameDetails),
            valueAttrs = listOf(valDetails)
        )

        val json = testInstance.writeValueAsString(attr)
        val expected = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "nameAttrs" to jsonArray({
                "name" to nameDetails.name
                "value" to nameDetails.value
            })
            "valueAttrs" to jsonArray({
                "name" to valDetails.name
                "value" to valDetails.value
            })
        }.toString()

        assertThat(json).isEqualToIgnoringWhitespace(expected)
    }
}
