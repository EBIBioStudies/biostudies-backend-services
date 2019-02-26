package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.common.createXmlDocument
import ac.uk.ebi.biostd.xml.deserializer.TestDeserializerFactory.Companion.linkXmlDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class LinkXmlDeserializerTest {
    private val testInstance = linkXmlDeserializer()

    @Test
    fun `deserialize link`() {
        val xmlLink = createXmlDocument(
            xml("file") {
                "url" { -"http://arandomurl.org" }
                "attributes" {
                    "attribute" {
                        "name" { -"attr1" }
                        "value" { -"attr 1 value" }
                    }
                }
            }.toString())

        assertThat(testInstance.deserialize(xmlLink)).isEqualTo(
            Link("http://arandomurl.org", mutableListOf(Attribute("attr1", "attr 1 value"))))
    }

    @Test
    fun `deserialize link without attributes`() {
        val xmlLink = createXmlDocument(
            xml("file") {
                "url" { -"http://arandomurl.org" }
            }.toString())

        assertThat(testInstance.deserialize(xmlLink)).isEqualTo(Link("http://arandomurl.org"))
    }
}
