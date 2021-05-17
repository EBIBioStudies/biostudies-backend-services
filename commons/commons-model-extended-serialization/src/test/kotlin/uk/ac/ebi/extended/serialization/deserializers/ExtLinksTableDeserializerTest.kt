package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtLinkTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize

class ExtLinksTableDeserializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val json = jsonObj {
            "links" to jsonArray(
                jsonObj {
                    "url" to "http://mylink.org"
                    "attributes" to jsonArray(
                        jsonObj {
                            "name" to "Type"
                            "value" to "Resource"
                        }
                    )
                    "extType" to "link"
                }
            )
            "extType" to "linksTable"
        }.toString()

        val extLinksTable = testInstance.deserialize<ExtLinkTable>(json)
        assertThat(extLinksTable.links).hasSize(1)

        val extLink = extLinksTable.links.first()
        assertThat(extLink.url).isEqualTo("http://mylink.org")
        assertThat(extLink.attributes).hasSize(1)
        assertThat(extLink.attributes.first().name).isEqualTo("Type")
        assertThat(extLink.attributes.first().value).isEqualTo("Resource")
    }
}
