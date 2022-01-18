package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.constants.FileFields
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class LinkJsonSerializerTest {
    private val testInstance = createSerializer()
    private val link = Link(url = "url", attributes = listOf(Attribute("attr name", "attr value")))

    @Test
    fun `serialize Link`() {
        val json = testInstance.writeValueAsString(link)
        val expected = jsonObj {
            "url" to "url"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "attr name"
                    "value" to "attr value"
                    "reference" to false
                }
            )
        }

        JSONAssert.assertEquals("invalid link json", json, expected.toString(), JSONCompareMode.LENIENT)
    }

    companion object {
        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(Link::class.java, LinkJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
