package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.JsonNull
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.SerializationProperties
import uk.ac.ebi.serialization.extensions.serialize

class ExtSectionsTableSerializerTest {
    private val testInstance = ExtSerializationService()

    @Test
    fun serialize() {
        val extSectionsTable = ExtSectionTable(listOf(ExtSection(type = "Study")))
        val expectedJson = jsonObj {
            "sections" to jsonArray(
                jsonObj {
                    "accNo" to JsonNull
                    "type" to "Study"
                    "fileList" to JsonNull
                    "attributes" to jsonArray()
                    "sections" to jsonArray()
                    "files" to jsonArray()
                    "links" to jsonArray()
                    "extType" to "section"
                }
            )
            "extType" to "sectionsTable"
        }.toString()

        assertThat(testInstance.serialize(extSectionsTable, SerializationProperties(includeFileListFiles = false)))
            .isEqualToIgnoringWhitespace(expectedJson)
    }
}
