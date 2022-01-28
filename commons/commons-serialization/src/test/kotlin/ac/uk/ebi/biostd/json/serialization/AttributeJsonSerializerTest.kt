package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.AttributeDetail
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class AttributeJsonSerializerTest {
    private val testInstance = createSerializer()
    private val valDetails = AttributeDetail("t1", "v1")
    private val nameDetails = AttributeDetail("t2", "v2")

    val attr = Attribute(
        name = "attr name",
        value = "attr value",
        reference = true,
        nameAttrs = mutableListOf(nameDetails),
        valueAttrs = mutableListOf(valDetails)
    )

    @Test
    fun `serialize attribute with empty value`() {
        val json = testInstance.writeValueAsString(Attribute(name = "attr name", value = ""))
        val expected = jsonObj {
            "name" to "attr name"
            "value" to null
            "reference" to false
        }.toString()

        JSONAssert.assertEquals("invalid attribute json", json, expected, JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize attribute with null value`() {
        val json = testInstance.writeValueAsString(Attribute(name = "attr name", value = null))
        val expected = jsonObj {
            "name" to "attr name"
            "value" to null
            "reference" to false
        }.toString()

        JSONAssert.assertEquals("invalid attribute json", json, expected, JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize attribute with blank value`() {
        val json = testInstance.writeValueAsString(Attribute(name = "attr name", value = "  "))
        val expected = jsonObj {
            "name" to "attr name"
            "value" to null
            "reference" to false
        }.toString()

        JSONAssert.assertEquals("invalid attribute json", json, expected, JSONCompareMode.LENIENT)
    }

    @Test
    fun `serialize attribute with details and reference`() {
        val json = testInstance.writeValueAsString(attr)
        val expected = jsonObj {
            "name" to attr.name
            "value" to attr.value
            "reference" to attr.reference
            "nmqual" to jsonArray({
                "name" to nameDetails.name
                "value" to nameDetails.value
            })
            "valqual" to jsonArray({
                "name" to valDetails.name
                "value" to valDetails.value
            })
        }.toString()

        JSONAssert.assertEquals("invalid attribute json", json, expected, JSONCompareMode.LENIENT)
    }

    companion object {
        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(Attribute::class.java, AttributeJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
