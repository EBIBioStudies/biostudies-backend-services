package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ExtLinksTableSerializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun serialize() {
        val extLinksTable =
            ExtLinkTable(
                listOf(
                    ExtLink(
                        url = "http://mylink.org",
                        attributes = listOf(ExtAttribute("Type", "Resource", false), ExtAttribute("Source", null, true)),
                    ),
                ),
            )
        val expectedJson =
            jsonObj {
                "links" to
                    jsonArray(
                        jsonObj {
                            "url" to "http://mylink.org"
                            "attributes" to
                                jsonArray(
                                    jsonObj {
                                        "name" to "Type"
                                        "value" to "Resource"
                                        "reference" to false
                                        "nameAttrs" to jsonArray()
                                        "valueAttrs" to jsonArray()
                                    },
                                    jsonObj {
                                        "name" to "Source"
                                        "value" to null
                                        "reference" to true
                                        "nameAttrs" to jsonArray()
                                        "valueAttrs" to jsonArray()
                                    },
                                )
                            "extType" to "link"
                        },
                    )
                "extType" to "linksTable"
            }.toString()

        assertThat(testInstance.writeValueAsString(extLinksTable)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
