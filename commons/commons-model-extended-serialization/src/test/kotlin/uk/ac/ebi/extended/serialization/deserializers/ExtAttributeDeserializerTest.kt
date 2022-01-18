package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ExtAttributeDeserializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `deserialize empty`() {
        val exception = assertThrows<IllegalStateException> { testInstance.readValue<ExtAttribute>("{}") }

        assertThat(exception.message).isEqualTo("Expecting to find property with 'name' in node '{}'")
    }

    @Test
    fun `deserialize null value`() {
        val jsonAttribute = jsonObj {
            "name" to "attr name"
            "value" to null
        }
        val attribute = testInstance.readValue<ExtAttribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize blank value`() {
        val jsonAttribute = jsonObj {
            "name" to "attr name"
            "value" to "  "
        }
        val attribute = testInstance.readValue<ExtAttribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize empty value`() {
        val jsonAttribute = jsonObj {
            "name" to "attr name"
            "value" to ""
        }
        val attribute = testInstance.readValue<ExtAttribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize no value`() {
        val jsonAttribute = jsonObj {
            "name" to "attr name"
        }
        val attribute = testInstance.readValue<ExtAttribute>(jsonAttribute.toString())

        assertThat(attribute.name).isEqualTo("attr name")
        assertThat(attribute.value).isNull()
    }

    @Test
    fun `deserialize with wrong type`() {
        val invalidJson = jsonObj {
            "name" to jsonArray(1, 2, 3)
        }.toString()

        val node = "{\"name\":[1,2,3]}"
        val exception = assertThrows<IllegalArgumentException> { testInstance.readValue<ExtAttribute>(invalidJson) }

        assertThat(exception.message).isEqualTo(
            "Expecting node: '$node', property: 'name' to be of type 'TextNode' but 'ArrayNode' was found instead"
        )
    }

    @Test
    fun `deserialize attribute with name and value`() {
        val attr = ExtAttribute(name = "attr name", value = "attr value", reference = false)
        val jsonAttribute = jsonObj {
            "name" to attr.name
            "value" to attr.value
        }
        val result = testInstance.readValue<ExtAttribute>(jsonAttribute.toString())

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with details and reference`() {
        val valDetails = ExtAttributeDetail("t1", "v1")
        val nameDetails = ExtAttributeDetail("t2", "v2")

        val attr = ExtAttribute(
            name = "attr name",
            value = "attr value",
            reference = true,
            nameAttrs = listOf(nameDetails),
            valueAttrs = listOf(valDetails)
        )

        val attributeJson = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "valueAttrs" to jsonArray({
                "name" to valDetails.name
                "value" to valDetails.value
            })
            "nameAttrs" to jsonArray({
                "name" to nameDetails.name
                "value" to nameDetails.value
            })
        }.toString()

        assertThat(testInstance.readValue<ExtAttribute>(attributeJson)).isEqualTo(attr)
    }
}
