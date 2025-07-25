package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AttributeJsonDeserializerTest {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize empty`() {
        val exception = assertThrows<IllegalStateException> { testInstance.readValue<Attribute>("{}") }

        assertThat(exception.message).isEqualTo("Expecting to find property with 'name' in node '{}'")
    }

    @Test
    fun `deserialize null value`() {
        val jsonAttribute =
            jsonObj {
                "name" to "attr name"
                "value" to null
            }
        val attribute = testInstance.readValue<Attribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize blank value`() {
        val jsonAttribute =
            jsonObj {
                "name" to "attr name"
                "value" to "  "
            }
        val attribute = testInstance.readValue<Attribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize empty value`() {
        val jsonAttribute =
            jsonObj {
                "name" to "attr name"
                "value" to ""
            }
        val attribute = testInstance.readValue<Attribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize no value`() {
        val jsonAttribute =
            jsonObj {
                "name" to "attr name"
            }
        val attribute = testInstance.readValue<Attribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize with wrong type`() {
        val invalidJson =
            jsonObj {
                "name" to jsonArray(1, 2, 3)
            }.toString()

        val node = "{\"name\":[1,2,3]}"
        val exception = assertThrows<IllegalArgumentException> { testInstance.readValue<Attribute>(invalidJson) }

        assertThat(exception.message).isEqualTo(
            "Expecting node: '$node', property: 'name' to be of type 'TextNode' but 'ArrayNode' was found instead",
        )
    }

    @Test
    fun `deserialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false)
        val jsonAttribute =
            jsonObj {
                "name" to attr.name
                "value" to attr.value
            }
        val result = testInstance.readValue<Attribute>(jsonAttribute.toString())

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with details and reference`() {
        val valDetails = AttributeDetail("t1", "v1")
        val nameDetails = AttributeDetail("t2", "v2")

        val attr =
            Attribute(
                name = "attr name",
                value = "attr value",
                reference = true,
                nameAttrs = mutableListOf(nameDetails),
                valueAttrs = mutableListOf(valDetails),
            )

        val attributeJson =
            jsonObj {
                "name" to attr.name
                "value" to attr.value
                "reference" to attr.reference
                "valqual" to
                    jsonArray({
                        "name" to valDetails.name
                        "value" to valDetails.value
                    })
                "nmqual" to
                    jsonArray({
                        "name" to nameDetails.name
                        "value" to nameDetails.value
                    })
            }.toString()

        assertThat(testInstance.readValue<Attribute>(attributeJson)).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with empty value qualifier value`() {
        val valDetails = AttributeDetail("t1", null)
        val nameDetails = AttributeDetail("t2", "v2")

        val attr =
            Attribute(
                name = "attr name",
                value = "attr value",
                reference = true,
                nameAttrs = mutableListOf(nameDetails),
                valueAttrs = mutableListOf(valDetails),
            )

        val attributeJson =
            jsonObj {
                "name" to attr.name
                "value" to attr.value
                "reference" to attr.reference
                "valqual" to
                    jsonArray({
                        "name" to valDetails.name
                        "value" to ""
                    })
                "nmqual" to
                    jsonArray({
                        "name" to nameDetails.name
                        "value" to nameDetails.value
                    })
            }.toString()

        assertThat(testInstance.readValue<Attribute>(attributeJson)).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with empty name qualifier value`() {
        val valDetails = AttributeDetail("t1", "v1")
        val nameDetails = AttributeDetail("t2", null)

        val attr =
            Attribute(
                name = "attr name",
                value = "attr value",
                reference = true,
                nameAttrs = mutableListOf(nameDetails),
                valueAttrs = mutableListOf(valDetails),
            )

        val attributeJson =
            jsonObj {
                "name" to attr.name
                "value" to attr.value
                "reference" to attr.reference
                "valqual" to
                    jsonArray({
                        "name" to valDetails.name
                        "value" to valDetails.value
                    })
                "nmqual" to
                    jsonArray({
                        "name" to nameDetails.name
                        "value" to ""
                    })
            }.toString()

        assertThat(testInstance.readValue<Attribute>(attributeJson)).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with empty name`() {
        val jsonAttribute =
            jsonObj {
                "name" to ""
                "value" to "ABC"
            }.toString()

        val exception = assertThrows<InvalidElementException> { testInstance.readValue<Attribute>(jsonAttribute) }
        assertThat(exception.message).isEqualTo("$REQUIRED_ATTR_NAME. Element was not created.")
    }
}
