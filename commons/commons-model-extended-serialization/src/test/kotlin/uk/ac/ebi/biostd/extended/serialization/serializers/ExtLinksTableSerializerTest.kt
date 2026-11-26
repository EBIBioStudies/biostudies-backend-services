package uk.ac.ebi.biostd.extended.serialization.serializers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.dsl.json.jsonArray
import uk.ac.ebi.biostd.dsl.json.jsonObj
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService

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
