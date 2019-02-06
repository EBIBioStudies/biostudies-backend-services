package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.junit.Test
import org.redundent.kotlin.xml.xml
import org.xmlunit.assertj.XmlAssert.assertThat

private const val ATTR_NAME = "color"
private const val ATTR_VALUE = "blue"
private val TERMS = mutableListOf(AttributeDetail("name", "value"))

class AttributeSerializerTest {

    private val testInstance = XmlSerializer.mapper

    private val attribute = Attribute(name = ATTR_NAME, value = ATTR_VALUE, valueAttrs = TERMS)

    @Test
    fun testSerializeAttribute() {
        val result = testInstance.writeValueAsString(attribute)
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

        val result = testInstance.writeValueAsString(attribute)
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
        attribute.valueAttrs = (attribute.valueAttrs + AttributeDetail("another", "another_value")).toMutableList()

        val result = testInstance.writeValueAsString(attribute)
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
