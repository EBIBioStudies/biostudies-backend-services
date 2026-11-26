package uk.ac.ebi.extended.serialization.serializers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.dsl.json.JsonNull
import uk.ac.ebi.biostd.dsl.json.jsonArray
import uk.ac.ebi.biostd.dsl.json.jsonObj
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSectionTable
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ExtSectionsTableSerializerTest {
    private val testInstance = ExtSerializationService()

    @Test
    fun serialize() {
        val extSectionsTable = ExtSectionTable(listOf(ExtSection(type = "Study")))
        val expectedJson =
            jsonObj {
                "sections" to
                    jsonArray(
                        jsonObj {
                            "accNo" to JsonNull
                            "type" to "Study"
                            "fileList" to JsonNull
                            "linkList" to JsonNull
                            "attributes" to jsonArray()
                            "sections" to jsonArray()
                            "files" to jsonArray()
                            "links" to jsonArray()
                            "extType" to "section"
                        },
                    )
                "extType" to "sectionsTable"
            }.toString()

        assertThat(testInstance.serializeElement(extSectionsTable)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
