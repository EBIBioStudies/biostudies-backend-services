package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.constants.FileFields
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class FileJsonSerializerTest {
    private val testInstance = createSerializer()
    val file = BioFile(
        path = "folder1/file.txt",
        size = 10L,
        type = FileFields.FILE.value,
        attributes = listOf(Attribute("name", "value"))
    )

    @Test
    fun `serialize File`() {
        val json = testInstance.writeValueAsString(file)
        val expected = jsonObj {
            "path" to "folder1/file.txt"
            "size" to 10L
            "type" to FileFields.FILE.value
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "name"
                    "value" to "value"
                    "reference" to false
                }
            )
        }

        JSONAssert.assertEquals("invalid file json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    companion object {
        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(BioFile::class.java, FileJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
