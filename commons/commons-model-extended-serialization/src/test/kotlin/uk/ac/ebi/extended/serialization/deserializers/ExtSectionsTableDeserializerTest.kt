package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSectionTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class ExtSectionsTableDeserializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val json = jsonObj {
            "sections" to jsonArray(
                jsonObj {
                    "type" to "Study"
                    "extType" to "section"
                }
            )
            "extType" to "sectionsTable"
        }.toString()

        val extSectionsTable = testInstance.readValue<ExtSectionTable>(json)
        assertThat(extSectionsTable.sections).hasSize(1)

        val extSection = extSectionsTable.sections.first()
        assertThat(extSection.accNo).isNull()
        assertThat(extSection.fileList).isNull()
        assertThat(extSection.files).isEmpty()
        assertThat(extSection.links).isEmpty()
        assertThat(extSection.sections).isEmpty()
        assertThat(extSection.type).isEqualTo("Study")
    }
}
