package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.XmlSerializer
import ac.uk.ebi.biostd.submission.Attribute
import ac.uk.ebi.biostd.submission.SimpleAttribute
import org.junit.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val ATTR_NAME = "color"
private const val ATTR_VALUE = "blue"
private val TERMS = listOf(SimpleAttribute("name", "value"))

class AttributeSerializerTest {

    private val testInstance = XmlSerializer()

    private val attribute = Attribute(name = ATTR_NAME, value = ATTR_VALUE, terms = TERMS)

    @Test
    fun testSerializeAttribute() {
        val result = testInstance.serialize(attribute)
        val expected = xml("attribute") {
            "name" { -ATTR_NAME }
            "value" { -ATTR_VALUE }
            "valqual" {
                "name" { -"name" }
                "value" { -"value" }
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun testSerializeAttributeWhenReference() {
        attribute.reference = true

        val result = testInstance.serialize(attribute)
        val expected = xml("attribute") {
            attribute("reference", true)
            "name" { -ATTR_NAME }
            "value" { -ATTR_VALUE }
            "valqual" {
                "name" { -"name" }
                "value" { -"value" }
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

    @Test
    fun testSerializeAttributeWhenMultipleTerms() {
        attribute.terms = attribute.terms.toMutableList() + SimpleAttribute("another", "another_value")

        val result = testInstance.serialize(attribute)
        val expected = xml("attribute") {
            "name" { -ATTR_NAME }
            "value" { -ATTR_VALUE }
            "valqual" {
                "name" { -"name" }
                "value" { -"value" }
            }
            "valqual" {
                "name" { -"another" }
                "value" { -"another_value" }
            }
        }.toString()

        assertThat(result).and(expected).ignoreWhitespace().areIdentical()
    }

}
