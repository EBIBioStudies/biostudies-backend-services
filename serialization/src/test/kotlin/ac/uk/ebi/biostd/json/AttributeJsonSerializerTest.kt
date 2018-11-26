package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.ext.deserialize
import ac.uk.ebi.biostd.ext.serialize
import ebi.ac.uk.dsl.jsonArray
import ebi.ac.uk.dsl.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class AttributeJsonSerializerTest {

    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize wrong structure`() {
        val exception = assertThrows<IllegalStateException> { testInstance.deserialize<Attribute>("{}") }

        assertThat(exception.message).isEqualTo("Expecting to find property with 'name' in node '{}'")
    }

    @Test
    fun `deserialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false)

        val result = testInstance.deserialize<Attribute>("""{
            |"name": "${attr.name}",
            |"value": "${attr.value}"
            |}""".trimMargin())

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with terms and reference`() {
        val term1 = AttributeDetail("t1", "v1")
        val term2 = AttributeDetail("t2", "v2")
        val attr = Attribute(name = "attr name", value = "attr value", reference = true, valueAttrs = mutableListOf(term1, term2))

        val attributeJson = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "valqual" to jsonArray({
                "name" to term1.name
                "value" to term1.value
            }, {
                "name" to term2.name
                "value" to term2.value
            })
        }.toString()

        assertThat(testInstance.deserialize<Attribute>(attributeJson)).isEqualTo(attr)
    }

    @Test
    fun `serialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false)

        val result = testInstance.deserialize<Attribute>(testInstance.serialize(attr))

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `serialize attribute with reference and terms`() {
        val attr = Attribute(
            name = "attr name",
            value = "attr value",
            reference = true,
            valueAttrs = mutableListOf(AttributeDetail("t1", "v1"), AttributeDetail("t2", "v2")))

        val result = testInstance.deserialize<Attribute>(testInstance.serialize(attr))
        assertThat(result).isEqualTo(attr)
    }
}
