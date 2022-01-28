package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.util.collections.second
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
                    "attributes" to jsonArray(
                        jsonObj {
                            "name" to "Source"
                            "reference" to true
                        }
                    )
                },
                jsonObj {
                    "type" to "Source"
                    "extType" to "section"
                    "attributes" to jsonArray(
                        jsonObj {
                            "name" to "Source"
                            "value" to null
                        }
                    )
                }
            )
            "extType" to "sectionsTable"
        }.toString()

        val extSectionsTable = testInstance.readValue<ExtSectionTable>(json)
        assertThat(extSectionsTable.sections).hasSize(2)

        val extSection = extSectionsTable.sections.first()
        assertThat(extSection.accNo).isNull()
        assertThat(extSection.fileList).isNull()
        assertThat(extSection.files).isEmpty()
        assertThat(extSection.links).isEmpty()
        assertThat(extSection.sections).isEmpty()
        assertThat(extSection.type).isEqualTo("Study")

        assertThat(extSection.attributes).hasSize(1)
        assertThat(extSection.attributes.first().name).isEqualTo("Source")
        assertThat(extSection.attributes.first().value).isNull()
        assertThat(extSection.attributes.first().reference).isTrue()

        val anotherSection = extSectionsTable.sections.second()
        assertThat(anotherSection.attributes).hasSize(1)
        assertThat(anotherSection.attributes.first().name).isEqualTo("Source")
        assertThat(anotherSection.attributes.first().value).isNull()
        assertThat(anotherSection.attributes.first().reference).isFalse()
    }
}
