package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.section
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Section
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT

class SectionJsonSerializerTest {
    private val testInstance = createSerializer()
    private val section = section("Study") {
        accNo = "SECT-001"
        fileList = FileList("file-list")
    }

    @Test
    fun `serialize section`() {
        val json = testInstance.writeValueAsString(section)
        val expected = jsonObj {
            "type" to "Study"
            "accno" to "SECT-001"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "File List"
                    "value" to "file-list.json"
                    "reference" to false
                }
            )
        }

        assertEquals("invalid submission json", json, expected.toString(), LENIENT)
    }

    companion object {
        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(Section::class.java, SectionJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
