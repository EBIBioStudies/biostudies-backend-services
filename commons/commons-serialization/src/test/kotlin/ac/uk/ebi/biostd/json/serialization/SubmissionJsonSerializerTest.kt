package ac.uk.ebi.biostd.json.serialization

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Submission
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.LENIENT
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
internal class SubmissionJsonSerializerTest {
    private val testInstance = createSerializer()

    private val releaseDate = LocalDate.of(2018, 12, 31).toString()
    private val submission =
        submission("abc123") {
            attribute("attr-name", "attr-value")
            attribute("releaseDate", releaseDate)
        }

    @Test
    fun `serialize local date`() {
        val json = testInstance.writeValueAsString(submission)
        val expected =
            jsonObj {
                "accno" to "abc123"
                "section" to jsonObj { }
                "attributes" to
                    jsonArray(
                        jsonObj {
                            "reference" to false
                            "name" to "attr-name"
                            "value" to "attr-value"
                        },
                        jsonObj {
                            "reference" to false
                            "name" to "releaseDate"
                            "value" to "2018-12-31"
                        },
                    )
                "type" to "submission"
            }
        assertEquals("invalid submission json", json, expected.toString(), LENIENT)
    }

    companion object {
        fun createSerializer(): ObjectMapper {
            val module = SimpleModule()
            module.addSerializer(Submission::class.java, SubmissionJsonSerializer())

            return jacksonObjectMapper().apply {
                registerModule(module)
                setSerializationInclusion(Include.NON_EMPTY)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }
}
