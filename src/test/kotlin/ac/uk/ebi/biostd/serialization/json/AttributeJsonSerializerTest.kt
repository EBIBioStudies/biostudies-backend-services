package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.Term
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AttributeJsonSerializerTest {

    @Test(expected = NullPointerException::class)
    fun `deserialize empty attribute`() {
        deserialize("{}")
    }

    @Test
    fun `deserialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false, terms = listOf())

        val result = deserialize("""{
            |"name": "${attr.name}",
            |"value": "${attr.value}"
            |}""".trimMargin())

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `deserialize attribute with terms and reference`() {
        val term1 = Term("t1", "v1")
        val term2 = Term("t2", "v2")
        val attr = Attribute(name = "attr name", value = "attr value", reference = true, terms = listOf(term1, term2))

        val result = deserialize("""{
            |"name": "${attr.name}",
            |"value": "${attr.value}",
            |"isReference": ${attr.reference},
            |"valqual": [{
            |    "name": "${term1.name}",
            |    "value": "${term1.value}"
            |},
            |{
            |    "name": "${term2.name}",
            |    "value": "${term2.value}"
            |}]}""".trimMargin())

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `serialize attribute with name and value`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = false, terms = listOf())

        val result = deserialize(serialize(attr))

        assertThat(result).isEqualTo(attr)
    }

    @Test
    fun `serialize attribute with reference and terms`() {
        val attr = Attribute(name = "attr name", value = "attr value", reference = true,
                terms = listOf(Term("t1", "v1"), Term("t2", "v2")))

        val result = deserialize(serialize(attr))

        assertThat(result).isEqualTo(attr)
    }

    private fun deserialize(json: String): Attribute = JsonSerializer().deserialize(json, Attribute::class.java)

    private fun serialize(attr: Attribute): String = JsonSerializer().serialize(attr)
}
