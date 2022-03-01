package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.XmlSerializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.redundent.kotlin.xml.xml

class LinkStandaloneXmlDeserializerTest {
    private val testInstance = XmlSerializer.mapper

    @Test
    fun `deserialize link`() {
        val xmlLink = xml("link") {
            "url" { -"http://arandomurl.org" }
            "attributes" {
                "attribute" {
                    "name" { -"attr1" }
                    "value" { -"attr 1 value" }
                }
            }
        }.toString()

        assertThat(testInstance.readValue(xmlLink, Link::class.java)).isEqualTo(
            Link("http://arandomurl.org", mutableListOf(Attribute("attr1", "attr 1 value")))
        )
    }

    @Test
    fun `deserialize link without attributes`() {
        val xmlLink = xml("link") {
            "url" { -"http://arandomurl.org" }
        }.toString()

        assertThat(testInstance.readValue(xmlLink, Link::class.java)).isEqualTo(Link("http://arandomurl.org"))
    }
}
