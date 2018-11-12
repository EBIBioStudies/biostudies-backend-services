package ac.uk.ebi.biostd.json

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import net.soundvibe.jkob.json
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AttributeJsonSerializerTest {

    @Test(expected = NullPointerException::class)
    fun `deserialize empty attribute`() {
        deserialize("{}")
    }

    @Test
    fun `deserialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false)

        val result = deserialize("""{
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

        val attributeJson = json {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "valqual"[ {
                "name" to term1.name
                "value" to term1.value
            }, {
                "name" to term2.name
                "value" to term2.value
            } ]
        }.toString()

        assertThat(deserialize(attributeJson)).isEqualTo(attr)
    }

    @Test
    fun `serialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false)

        val result = deserialize(serialize(attr))

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `serialize attribute with reference and terms`() {
        val attr = Attribute(
                name = "attr name",
                value = "attr value",
                reference = true,
                valueAttrs = mutableListOf(AttributeDetail("t1", "v1"), AttributeDetail("t2", "v2")))

        val result = deserialize(serialize(attr))
        assertThat(result).isEqualTo(attr)
    }

    private fun deserialize(json: String): Attribute = JsonSerializer().deserialize(json, Attribute::class.java)

    private fun serialize(attr: Attribute): String = JsonSerializer().serialize(attr)
}
