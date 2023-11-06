package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ac.uk.ebi.biostd.xml.common.createXmlDocument
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.redundent.kotlin.xml.xml

class AttributeXmlDeserializerTest {
    private val testInstance = AttributeXmlDeserializer(DetailsXmlDeserializer())

    @Test
    fun `deserialize attribute`() {
        val xmlAttribute = createXmlDocument(
            xml("attribute") {
                "name" { -"attr1" }
                "value" { -"value1" }
                "nmqual" {
                    "name" { -"nameDetail" }
                    "value" { -"name detail 1" }
                }
                "valqual" {
                    "name" { -"valDetail" }
                    "value" { -"val detail 1" }
                }
            }.toString()
        )

        assertThat(testInstance.deserialize(xmlAttribute)).isEqualTo(
            Attribute(
                name = "attr1",
                value = "value1",
                reference = false,
                nameAttrs = mutableListOf(AttributeDetail("nameDetail", "name detail 1")),
                valueAttrs = mutableListOf(AttributeDetail("valDetail", "val detail 1"))
            )
        )
    }

    @Test
    fun `deserialize with empty attribute`() {
        val xmlAttribute = createXmlDocument(
            xml("attribute") {
                "name" { -"attr1" }
            }.toString()
        )

        assertThat(testInstance.deserialize(xmlAttribute)).isEqualTo(
            Attribute(
                name = "attr1",
                value = null,
                reference = false,
                nameAttrs = mutableListOf(),
                valueAttrs = mutableListOf()
            )
        )
    }

    @Test
    fun `deserialize reference attribute`() {
        val xmlAttribute = createXmlDocument(
            xml("attribute") {
                attribute("reference", true)
                "name" { -"Organization" }
                "value" { -"Org1" }
            }.toString()
        )

        assertThat(testInstance.deserialize(xmlAttribute)).isEqualTo(Attribute("Organization", "Org1", true))
    }

    @Test
    fun `deserialize attribute with empty name`() {
        val xmlAttribute = createXmlDocument(
            xml("attribute") {
                "name" { -"" }
                "value" { -"ABC" }
            }.toString()
        )

        val exception = assertThrows<InvalidElementException> { testInstance.deserialize(xmlAttribute) }
        assertThat(exception.message).isEqualTo("$REQUIRED_ATTR_NAME. Element was not created.")
    }
}
