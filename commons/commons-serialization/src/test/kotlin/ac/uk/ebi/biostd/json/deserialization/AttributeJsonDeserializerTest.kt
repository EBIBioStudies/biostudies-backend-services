package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.ac.ebi.serialization.extensions.deserialize

class AttributeJsonDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize empty`() {
        val exception = assertThrows<IllegalStateException> { testInstance.deserialize<Attribute>("{}") }

        assertThat(exception.message).isEqualTo("Expecting to find property with 'name' in node '{}'")
    }

    @Test
    fun `deserialize empty value`() {
        val attribute = testInstance.deserialize<Attribute>(
            """{
            |"name": "attr name",
            |"value": ""
            |}""".trimMargin()
        )

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isEmpty()
    }

    @Test
    fun `deserialize no value`() {
        val attribute = testInstance.deserialize<Attribute>(
            """{
            |"name": "attr name"
            |}""".trimMargin()
        )

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isEmpty()
    }

    @Test
    fun `deserialize with wrong type`() {
        val invalidJson = jsonObj {
            "name" to jsonArray(1, 2, 3)
        }.toString()

        val node = "{\"name\":[1,2,3]}"
        val exception = assertThrows<IllegalArgumentException> { testInstance.deserialize<Attribute>(invalidJson) }

        assertThat(exception.message).isEqualTo(
            "Expecting node: '$node', property: 'name' to be of type 'TextNode' but 'ArrayNode' was found instead"
        )
    }

    @Test
    fun `deserialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false)

        val result = testInstance.deserialize<Attribute>(
            """{
            |"name": "${attr.name}",
            |"value": "${attr.value}"
            |}""".trimMargin()
        )

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with details and reference`() {
        val valDetails = AttributeDetail("t1", "v1")
        val nameDetails = AttributeDetail("t2", "v2")

        val attr = Attribute(
            name = "attr name",
            value = "attr value",
            reference = true,
            nameAttrs = mutableListOf(nameDetails),
            valueAttrs = mutableListOf(valDetails)
        )

        val attributeJson = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "valqual" to jsonArray({
                "name" to valDetails.name
                "value" to valDetails.value
            })
            "nmqual" to jsonArray({
                "name" to nameDetails.name
                "value" to nameDetails.value
            })
        }.toString()

        assertThat(testInstance.deserialize<Attribute>(attributeJson)).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with empty value qualifier value`() {
        val valDetails = AttributeDetail("t1", "v1")
        val nameDetails = AttributeDetail("t2", "v2")

        val attr = Attribute(
            name = "attr name",
            value = "attr value",
            reference = true,
            nameAttrs = mutableListOf(nameDetails),
            valueAttrs = mutableListOf(valDetails)
        )

        val attributeJson = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "valqual" to jsonArray({
                "name" to valDetails.name
                "value" to ""
            })
            "nmqual" to jsonArray({
                "name" to nameDetails.name
                "value" to nameDetails.value
            })
        }.toString()

        assertThrows<IllegalArgumentException> { testInstance.deserialize<Attribute>(attributeJson) }
    }

    @Test
    fun `deserialize attribute with empty name qualifier value`() {
        val valDetails = AttributeDetail("t1", "v1")
        val nameDetails = AttributeDetail("t2", "v2")

        val attr = Attribute(
            name = "attr name",
            value = "attr value",
            reference = true,
            nameAttrs = mutableListOf(nameDetails),
            valueAttrs = mutableListOf(valDetails)
        )

        val attributeJson = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "valqual" to jsonArray({
                "name" to valDetails.name
                "value" to valDetails.value
            })
            "nmqual" to jsonArray({
                "name" to nameDetails.name
                "value" to ""
            })
        }.toString()

        assertThrows<IllegalArgumentException> { testInstance.deserialize<Attribute>(attributeJson) }
    }
}
